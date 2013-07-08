public long generateQuote(BookingRequestDataObject requestDataObject) {
   
    Customer customer  = customerDataAccess.get(requestDataObject.getCustomerId());
   
    QuoteHeader quoteHeader = new QuoteHeader();
    quoteHeader.setCustomerId(customer.getId());
    quoteHeader.setDate(new Date());
    long quoteHeaderId = quoteHeaderDataAccess.save(quoteHeader);
    
    List<Room> rooms = roomDataAccess.find(requestDataObject.getCheckinDate(), requestDataObject.getCheckoutDate());
    if (rooms == null) {
        return null;
    }
    
    List<Date> dates = datesStayingHelper(requestDataObject.getCheckinDate(), requestDataObject.getCheckoutDate());
    
    for (int i = 0; i < requestDataObject.getNumberOfRooms(); i++) {
        Room room = rooms.get(i);
        for (Date date : dates) {
            QuoteDetail detail = new QuoteDetail();
            detail.setQuoteHeaderId(quoteHeaderId);
            detail.setRoomId(room.getId());
            detail.setDate(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            switch(calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                case Calendar.TUESDAY:
                case Calendar.WEDNESDAY:
                case Calendar.THURSDAY:
                    detail.setRate(room.getWeekdayRate());
                    break;
                case Calendar.FRIDAY:
                case Calendar.SATURDAY:
                case Calendar.SUNDAY:
                    detail.setRate(room.getWeekendRate());;
                    break;
            }
            quoteDetailDataAccess.save(detail);
        }
    }
    
    if (requestDataObject.getSelectedSpecialOfferIds().length > 0) {
        for (Long id : requestDataObject.getSelectedSpecialOfferIds()) {
            SpecialOffer specialOffer = specialOfferDataAccess.get(id);
            QuoteSpecialOffer quoteSpecialOffer = new QuoteSpecialOffer();
            quoteSpecialOffer.setQuoteHeaderId(quoteHeaderId);
            quoteSpecialOffer.setSpecialOfferId(id);
            quoteSpecialOffer.setRate(specialOffer.getRate());
            quoteSpecialOfferDataAccess.save(quoteSpecialOffer);
        }
    }
    
    return quoteHeaderId;
}