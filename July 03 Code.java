// Later: Quote is a more appropriate object than QuoteHeader
// Could refactor requestDataObject to directly return a date interval
public QuoteHeader createQuote(BookingRequestDataObject requestDataObject) {
   
    Customer customer  = customerDataAccess.get(requestDataObject.getCustomerId());
   
    QuoteHeader quoteHeader = new QuoteHeader();
    quoteHeader.setCustomer(customer);
    quoteHeader.setDate(new Date());
    long quoteHeaderId = quoteHeaderDataAccess.save(quoteHeader);
    
    DateInterval dateInterval = new DateInterval(requestDataObject.getCheckinDate(), requestDataObject.getCheckoutDate());

    List<Room> rooms = roomDataAccess.find(dateInterval);
    if (rooms == null) {
        return null;
    }
    
    List<Date> dates = datesStayingHelper(dateInterval);
    
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
    
    return quoteHeader;
}