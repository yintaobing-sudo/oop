import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EventManagementSystem {
 
    private List<Ticket> tickets = new java.util.ArrayList<>();
    private Payment[] payments = new Payment[100];
    private final int MAX_EVENTS = 300;
    private Event[] events = new Event[MAX_EVENTS];
    
    private int eventCount = 0;

    private int user_no = 0;
    private User current_user;
    private User[] user = new User[400];

    // get methods for Events
    // Get all events across every type (flat array)

    public EventManagementSystem(User[] user, Event[] events, List<Ticket> tickets, Payment[] payments) {
        this.user = user;
        this.events = events;
        this.eventCount=getCountEvent();

        this.tickets = tickets;
        this.payments = payments;

        this.user_no = countUser_Num();
        //count the how many booking no have already store in the file
        //no do the payment file, so use the ticket to count , because the total ticket is equals to total booking
        Payment.setbookingNo(countBookingNo());

    }

    public EventManagementSystem() {
        this(null, null, null, null);
    }

    // Getter Method

    public User[] getUsers() {
        return user;
    }

    public User getCuurent_User() {
        return current_user;
    }

    public int getUser_No(){
        return this.user_no;
    }

    public void setCuurent_User(User current_User, int type) {
        switch (type) {
            case 1:
                this.current_user = (Organizer) current_User;
                break;
            case 2:
                this.current_user = (Speaker) current_User;

                break;
            case 3:
                this.current_user = (Staff) current_User;

                break;
            case 4:
                this.current_user = (Attendee) current_User;
                break;
        }

    }

    public int getCountEvent(){
        int count=0;
        for (Event event : events){
            if(event !=null){
                count++;
            }
        }
        return count;
    }

    public int countBookingNo(){
        int count=0;
        for (Ticket ticket : tickets){
            if (ticket !=null){
                count++;

            }
        }
            return count;
    }

    public int countUser_Num() {
        for (User current_user : user) {
            if (current_user != null) {
                user_no++;
            }

        }
        return user_no;
    }

    // Get a single event by eventID (searches all types)
    public Event getEventById(String eventID) {
        for (int type = 0; type < 3; type++) {
            for (int i = 0; i < eventCount; i++) {
                if (events[i] != null && events[i].hasEvent(eventID)) {
                    return events[i];
                }
            }
        }
        System.out.println("Error: Event [" + eventID + "] not found !");
        return null;
    }

    // ------------------------------------------------------------------------Get
    // event end here

    // ==================================User part==============================
    public void addNewUser(User user) {
        this.user[user_no] = user;
        this.user_no++;

    }

    // -------------------------- validation for event ---------------------------

    // validate date string (must be YYYY-MM-DD format and a future date)
    // returns the parsed LocalDate if valid, or null if invalid
    public LocalDate validationDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            System.out.println("Error: Date cannot be empty !");
            return null;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(date.trim());
            if (!parsedDate.isAfter(LocalDate.now())) {
                System.out.println("Error: Event date must be in the future !");
                return null;
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            System.out.println("Error: Date format must be YYYY-MM-DD !");
            return null;
        }
    }

    public boolean validationInputEventId(String eventId) {
        for (Event e : this.events) {
            if (e != null && e.hasEvent(eventId)) {
                return true;
            }

        }

        return false;
    }

    // -------------------------- create event ---------------------------

    // Creates a Concert and stores it in events[CONCERT][]
    public Concert createConcert(String title, String date, String venue, int maxTickets) {
        LocalDate parsedDate = validationDate(date);
        Concert c = new Concert(title, parsedDate, venue, maxTickets);
        events[eventCount] = c;
        eventCount++;
        System.out.println("Concert created and stored successfully : " + c.getEventID());
        return c;
    }

    // Creates a Workshop and stores it in events[WORKSHOP][]
    public Workshop createWorkshop(String title, String date, String venue, int maxTickets) {
        LocalDate parsedDate = validationDate(date);
        Workshop w = new Workshop(title, parsedDate, venue, maxTickets);
        events[eventCount] = w;
        eventCount++;
        System.out.println("Workshop created and stored successfully : " + w.getEventID());
        return w;
    }

    // Creates a Conference (with optional sessions) and stores it in
    // events[CONFERENCE][]
    public Conference createConference(String title, String date, String venue, int maxTickets,
            String[] sessionTopics, String[] sessionTimes) {
        LocalDate parsedDate = validationDate(date);
        if (parsedDate == null)
            return null;

        Conference conf = new Conference(title, parsedDate, venue, maxTickets);
        if (sessionTopics != null && sessionTimes != null) {
            conf.autoCreateSessions(sessionTopics, sessionTimes);
        }
        events[eventCount] = conf;
        eventCount++;
        System.out.println("Conference created and stored successfully : " + conf.getEventID());
        return conf;
    }

    // Overload — create Conference without sessions
    public Conference createConference(String title, String date, String venue, int maxTickets) {
        return createConference(title, date, venue, maxTickets, null, null);
    }

    // -------------------------- assign speaker ---------------------------

    public boolean assignSpeaker(Session session, Speaker speaker, Conference conf) {
        if (session == null) {
            System.out.println("Error: Session not found !");
            return false;
        }
        if (speaker == null) {
            System.out.println("Error: Speaker not found !");
            return false;
        }
        if (session.addSpeaker(speaker.getUsername())) {
            System.out.println("Speaker [" + speaker.getUsername() + "] assigned to session ["
                    + session.getSessionID() + "] successfully.");
            return true;
        }
        return false;
    }

    public boolean removeSpeaker(Session session, String speakerUsername) {
        if (session == null) {
            System.out.println("Error: Session not found !");
            return false;
        }

        return session.removeSpeaker(speakerUsername);
    }

    // Upload/update session topic
    public boolean uploadSessionTopic(String username, Session session, String newTopic) {
        // Session.getSpeakers() returns String[] of usernames
        String[] assigned = session.getSpeakers();
        for (int i = 0; i < session.getSpeakerCount(); i++) {
            if (assigned[i] != null && assigned[i].equals(username)) {
                String status = session.getSpeakerStatus(username);
                if ("accepted".equals(status)) {
                    session.setTopic(newTopic);
                    System.out.println("Session [" + session.getSessionID()
                            + "] topic updated to: \"" + newTopic
                            + "\" by speaker: " + username);
                    return true;
                } else if ("pending".equals(status)) {
                    System.out.println(
                            "You need to accept the session first before updating the topic.");
                    return false;
                } else {
                    System.out.println(
                            "You have rejected this session. Cannot update topic.");
                    return false;
                }
            }
        }
        System.out.println("Speaker [" + username + "] is not assigned to session ["
                + session.getSessionID() + "]. Cannot update topic.");
        return false;
    }
 
    public Event findEventById(String eventId) {
        for (Event e : this.events) {
            if (e != null && e.hasEvent(eventId)) {
                return e;
            }
        }

        return null;

    }

    public Ticket purchaseTicket(TicketType tt, String eventId, String ticketTypeName, Payment payment,
            int ticketCount) {
        if (tt == null) {
            System.out.println("Error: TicketType cannot be null!");
            return null;
        }

        Ticket ticket = new Ticket(tt, current_user.getUsername(), ticketTypeName, true, eventId, ticketCount,
                payment.getBookingId());
        tickets.add(ticket);
        payments[tickets.size()-1]=payment;
        return ticket;
    }

    public String toString() {
        return "User\t:" + current_user.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof EventManagementSystem) {
            return true;
        }else{
            return false;
        }
    }
}
