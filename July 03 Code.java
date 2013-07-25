// Could refactor requestDataObject to directly return a date interval
// Transactional save to deal with potential unsuccessful save. Assuming the happy path currently
public Quote createQuote(BookingRequestDataObject requestDataObject) {
    Quote quote = new Quote();

    Customer customer  = customerDataAccess.get(requestDataObject.getCustomerId());
    quote.setCustomer(customer);
    
    quote.setDate(new Date());
    buildQuoteDetails(quote, requestDataObject);
    buildSpecialOffer(quote, requestDataObject);   

    return quote;
}

private QuoteDetail createQuoteDetail(Room room, Date date){
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
    return detail;
}

private void buildQuoteDetails(Quote quote, BookingRequestDataObject requestDataObject){
    DateInterval dateInterval = new DateInterval(requestDataObject.getCheckinDate(), requestDataObject.getCheckoutDate());
    
    List<Room> rooms = roomDataAccess.find(dateInterval);
    if (rooms == null) {
        return null;
    }
    
    List<Date> dates = datesStayingHelper(dateInterval);

    for (int i = 0; i < requestDataObject.getNumberOfRooms(); i++) {
        Room room = rooms.get(i);
        for (Date date : dates) {
            QuoteDetail detail = createQuoteDetail(room, date);
 
            quoteDetailDataAccess.save(detail);

            quote.AddQuoteDetails(detail);
        }
    }
}

private void buildSpecialOffer(Quote quote, BookingRequestDataObject requestDataObject){
    if (requestDataObject.getSelectedSpecialOfferIds().length > 0) {
        for (Long id : requestDataObject.getSelectedSpecialOfferIds()) {
            SpecialOffer specialOffer = specialOfferDataAccess.get(id);
            QuoteSpecialOffer quoteSpecialOffer = new QuoteSpecialOffer();
            quoteSpecialOffer.setSpecialOfferId(id);
            quoteSpecialOffer.setRate(specialOffer.getRate());
            quoteSpecialOfferDataAccess.save(quoteSpecialOffer);
            quote.AddSpecialOffers(quoteSpecialOffer);
        }
    }   
}