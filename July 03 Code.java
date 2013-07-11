// Later: Quote is a more appropriate object than QuoteHeader
// Could refactor requestDataObject to directly return a date interval
// Transactional save to deal with potential unsuccessful save. Assuming the happy path currently
public QuoteHeader createQuote(BookingRequestDataObject requestDataObject) {
   
    Customer customer  = customerDataAccess.get(requestDataObject.getCustomerId());
   
    QuoteHeader quoteHeader = new QuoteHeader();
    quoteHeader.setCustomer(customer);
    quoteHeader.setDate(new Date());
    
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

            quoteHeader.AddQuoteDetails(detail);
        }
    }
    

    if (requestDataObject.getSelectedSpecialOfferIds().length > 0) {
        for (Long id : requestDataObject.getSelectedSpecialOfferIds()) {
            SpecialOffer specialOffer = specialOfferDataAccess.get(id);
            QuoteSpecialOffer quoteSpecialOffer = new QuoteSpecialOffer();
            quoteSpecialOffer.setSpecialOfferId(id);
            quoteSpecialOffer.setRate(specialOffer.getRate());
            quoteSpecialOfferDataAccess.save(quoteSpecialOffer);
            quoteHeader.AddSpecialOffers(quoteSpecialOffer);
        }
    }
    
    return quoteHeader;
}