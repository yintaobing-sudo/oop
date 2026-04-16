import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class TestUser {
    static Scanner scan = new Scanner(System.in);
    static EventManagementSystem ems = null;

    // Safe integer reader
    static int readInt() {
        while (true) {
            try {
                int val = scan.nextInt();
                scan.nextLine();
                return val;
            } catch (Exception e) {
                scan.nextLine(); // clear bad token
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    static List<TicketType> ticketTypes = new java.util.ArrayList<>();
    static List<Ticket> tickets = new java.util.ArrayList<>();
    static int ticketCount = 0;
    static int bookingNo;

    // in-memory lists loaded from / saved to JSON files
    static List<Concert> concerts = new java.util.ArrayList<>();
    static List<Workshop> workshops = new java.util.ArrayList<>();
    static List<Conference> conferences = new java.util.ArrayList<>();

    static Event[] events = new Event[300];
    static int eventCount = 0;

    // speaker pool
    static Speaker[] speakerPool = new Speaker[100];
    static int speakerCount = 0;

    static Organizer organizer;
    static Attendee attendee;

    static Payment[] payments = new Payment[100];

    static final String ORGANIZER_PSWD = "org@2026";
    static final String SPEAKER_PSWD = "spk@2026";
    static final String STAFF_PSWD = "stf@2026";

    public static void main(String[] args) {
        // data store for user

        User[] alluser = new User[400];
        // no set as array to pass the value and change the value (only reference
        // variable will be affect)
        int[] no = { 0 };
        // no is from 0 to 99
        // load data

        readUserData(no, alluser); // load all user into the alluser array
        loadSpeakersFromUsers(alluser, no[0]); // populate speakerPool from Speaker accounts in user.json
        loadAllEvents(); // load all events and ticket types from files on startup
        tickets.clear();
        tickets = readTicketFile();

        // check the ticket status
        Staff.checkTotal_CheckIn(tickets);

        ticketCount = tickets.size();
        ems = new EventManagementSystem(alluser, events, tickets, payments);

        boolean active = true;
        while (active) {
            clearScreen();
            int option = displayAccessInterface();
            User user = new User();
            clearScreen();

            switch (option) {
                case 1:
                    // login
                    if (!displayLoginInterface(user, alluser, no)) {
                        break;
                    }
                    // set the access user
                    accessmenu(user, alluser, no);
                    break;

                case 2:
                    // signup

                    displaySignUpInterface(user, alluser, no);
                    // set the access user

                    accessmenu(user, alluser, no);
                    break;
                case 0:
                    System.out.println("            /\\_/\\  ");
                    System.out.println("           ( ^.^ ) ");
                    System.out.println("            > ^ <   See You Again!");
                    System.out.println("                      GOOD BYE ");
                    active = false;
                    break;
                default:
                    System.out.println("Error: Please Select The Correct Number");

            }
        }

    }

    // to select the method to access the system
    public static int displayAccessInterface() {
        int option = -1;
        boolean success = true;
        do {
            try {
                System.out.println("\t\t\t==============================================");
                System.out.println("\t\t\t          EVENT MANAGEMENT SYSTEM");
                System.out.println("\t\t\t==============================================");
                System.out.println("\t\t\t      EEEEE   V   V   EEEEE   N   N   TTTTT");
                System.out.println("\t\t\t      E       V   V   E       NN  N     T");
                System.out.println("\t\t\t      EEEE    V   V   EEEE    N N N     T");
                System.out.println("\t\t\t      E       V   V   E       NN  N     T");
                System.out.println("\t\t\t      EEEEE     V     EEEEE   N   N     T");
                System.out.println("\t\t\t==============================================");

                System.out.print("\n \t\t\t\t1. Login");
                System.out.print("\n \t\t\t\t2. Sign Up");
                System.out.println("\n \t\t\t\t0. Exit");
                System.out.print("\n\t\t\tEnter Your Option:\t");
                option = scan.nextInt(); // Use class-level scan, not local
                scan.nextLine();

                if (option > 2 || option < 0) {
                    success = false;
                    System.out.println("Input Error: Please Select 0, 1 or 2!");
                }
            } catch (Exception e) {
                System.out.println("Error: Existing Unknown Character!");
                scan.nextLine(); // Clear the buffer
                option = -1;
                success = false;
            }
        } while (option > 2 || option < 0);
        return option;
    }

    public static boolean displayLoginInterface(User user, User[] alluser, int[] no) {
        int logincount = 0;
        String name;
        String password;
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("|                       LOGIN SYSTEM                           |");
        System.out.println("----------------------------------------------------------------");
        do {
            if (logincount > 2) {
                return false;
            }
            logincount++;
            System.out.print("\nPlease enter your name: ");
            name = scan.nextLine();
            user.setUserName(name.strip());

        } while (!validationNoExistName(user, alluser, no));

        do {

            System.out.print("\nPlease enter your Password: ");
            password = scan.nextLine();

        } while (!validationLoginPwd(password, alluser, no));
        user.setUserName(name.strip());
        System.out.println("------------------------------------------------------------");
        return true;
    }

    public static void displaySignUpInterface(User user, User[] alluser, int[] no) {
        String name;
        String email;
        String contactNo;
        String password;
        String password2;
        String bio = "No bio available";
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("|                       SIGN UP SYSTEM                         |");
        System.out.println("----------------------------------------------------------------");
        do {

            System.out.print("\nPlease enter your name: ");
            name = scan.nextLine();
        } while (!validationName(name) || !validationExist(name, alluser));

        do {

            System.out.print("\nPlease enter your email: ");
            email = scan.nextLine();

        } while (!validationEmail(email));

        do {

            System.out.print("\nPlease enter your Contact Number [eg. 01113018399]: ");
            contactNo = scan.nextLine();

        } while (!validationcontactNo(contactNo));

        do {

            System.out.print("\nPlease enter your Password: ");
            password = scan.nextLine();

        } while (!validationPassword(password));

        do {

            System.out.print("\nPlease enter your comfirm password: ");
            password2 = scan.nextLine();

        } while (!validationPassword2(password, password2));

        // Ask for bio only if signing up as speaker
        if (password.equals(SPEAKER_PSWD)) {
            System.out.print("\nPlease enter your Bio (optional): ");
            bio = scan.nextLine();
            if (bio == null || bio.trim().isEmpty()) {
                bio = "No bio available";
            }
        }

        System.out.println("------------------------------------------------------------");

        // create and store data
        user.setUserName(name);
        createAccount(no, user, alluser, name, password, email, contactNo);
        storeUserData(name, password, email, contactNo);

        // Save bio to speaker.json if speaker
        if (password.equals(SPEAKER_PSWD)) {
            saveSpeakerBio(name, bio);
        }

    }

    public static void accessmenu(User user, User[] alluser, int[] no) {
        // Find the actual stored user object by username
        User storedUser = null;
        for (int i = 0; i < ems.getUser_No(); i++) {
            if (alluser[i].hasUser(user.getUsername())) {
                storedUser = alluser[i];
                break;
            }
        }
        if (storedUser == null)

            storedUser = alluser[no[0]]; // fallback

        System.out.println("-------------------------------------------------------------\n" +
                "|                   Access Successful !!!                   |\n" +
                "------------------------------------------------------------|\n" +
                "|                                                           |\n" +
                "|                                                           |");
        if (storedUser.getPassword().equals(ORGANIZER_PSWD)) {
            Organizer organizer = (Organizer) storedUser;
            ems.setCuurent_User(organizer, 1);
            System.out.println(organizer.toString() +
                    "|                                                           |\n" +
                    "-------------------------------------------------------------");
            System.out.println("Please Click Enter To continue...");

            waitForEnter();
            organizerMenu();
        } else if (storedUser.getPassword().equals(SPEAKER_PSWD)) {
            Speaker speaker = (Speaker) storedUser;
            ems.setCuurent_User(speaker, 2);
            System.out.println(speaker.toString() +
                    "|                                                           |\n" +
                    "-------------------------------------------------------------");
            System.out.println("Please Click Enter To continue...");

            waitForEnter();
            speakerMenu(speaker);
        } else if (storedUser.getPassword().equals(STAFF_PSWD)) {
            Staff staff = (Staff) storedUser;
            System.out.println(staff.toString() +
                    "|                                                           |\n" +
                    "-------------------------------------------------------------");
            System.out.println("Please Click Enter To continue...");

            ems.setCuurent_User(staff, 3);
            waitForEnter();
            staffMenu(staff, alluser, no);
        } else {
            int count = 0;
            Attendee attendee = (Attendee) storedUser;

            for (Ticket t : tickets) {
                if (attendee.hasUser(t.getBuyerName())) {
                    attendee.addToTotalSpent(t.getTotalAmount());
                    count++;
                }
            }
            attendee.setEventCount(count);

            System.out.println(attendee.toString() +
                    "|                                                           |\n" +
                    "-------------------------------------------------------------");

            ems.setCuurent_User(attendee, 4);
            System.out.println("Please Click Enter To continue...");
            waitForEnter();
            attendeeMenu(attendee);
        }
    }

    // load the data from user.json
    public static void readUserData(int[] no, User[] alluser) {

        // Read user data from the file and populate the arrays
        try {

            List<String> lines = Files.readAllLines(Paths.get("user.json"));
            // check the file is empty or not
            if (lines.isEmpty()) {

                // STORE Data
            } else {
                int size = (lines.size() / 4);
                // store data into 2D array
                String[][] information = new String[size][4];

                for (int i = 0; i < lines.size(); i += 1) {

                    // for username
                    if (i % 4 == 0) {
                        information[i / 4][0] = lines.get(i);
                    }

                    // for password
                    if ((i) % 4 == 1) {
                        information[i / 4][1] = lines.get(i);
                    }

                    // for email
                    if ((i) % 4 == 2) {
                        information[(i / 4)][2] = lines.get(i);
                    }
                    // for contact Number
                    if ((i) % 4 == 3) {
                        information[(i / 4)][3] = lines.get(i);
                    }

                }

                for (int i = 0; i < size; i++) {

                    String current_pwd = information[i][1];
                    // to store different user type data
                    if (current_pwd.equals(ORGANIZER_PSWD)) {

                        // get the no of last user

                        alluser[no[0]] = new Organizer(information[i][0], information[i][1], information[i][2],
                                information[i][3]);
                        no[0]++;
                    } else if (current_pwd.equals(SPEAKER_PSWD)) {

                        alluser[no[0]] = new Speaker(information[i][0], information[i][1], information[i][2],
                                information[i][3]);
                        no[0]++;

                    } else if (current_pwd.equals(STAFF_PSWD)) {
                        alluser[no[0]] = new Staff(information[i][0], information[i][1], information[i][2],
                                information[i][3]);
                        no[0]++;
                    } else {

                        alluser[no[0]] = new Attendee(information[i][0], information[i][1], information[i][2],
                                information[i][3]);
                        no[0]++;
                    }

                }

                // get the latest no index for each user type
                for (int i = 0; i < 0; i++) {

                    // if no any record , do not need to minus
                    if (no[i] != 0) {

                        no[i]--;

                    }

                }

            } // create user file
        } catch (IOException e) {
            createUserFile();
        }

    }

    public static void storeUserData(String username, String password, String email, String contactNo) {
        // append the new user data to user.json
        // if user.json is not exist, create it first
        try (Writer writer = new java.io.FileWriter("user.json", true)) {
            // avoid store null data into file
            if (username != null && password != null && email != null) {
                writer.write(username + "\n");
                writer.write(password + "\n");
                writer.write(email + "\n");
                writer.write(contactNo + "\n");
            }

        } catch (IOException e) {
            System.out.println("Error storing user data: " + e.getMessage());
        }
    }

    public static void createAccount(int[] no, User user, User[] alluser, String name, String password, String email,
            String contactNo) {

        if (password.equals(ORGANIZER_PSWD)) {

            alluser[no[0]] = new Organizer(name, password, email, contactNo);

        } else if (password.equals(SPEAKER_PSWD)) {

            alluser[no[0]] = new Speaker(name, password, email, contactNo);

        } else if (password.equals(STAFF_PSWD)) {

            alluser[no[0]] = new Staff(name, password, email, contactNo);

        } else {

            alluser[no[0]] = new Attendee(name, password, email, contactNo);

        }
        ems.addNewUser(alluser[no[0]]);

    }

    // create user file if no exist
    public static void createUserFile() {
        try {
            File user = new File("user.json");
            if (user.createNewFile()) {
                System.out.println("Please Waiting...");
                System.out.println("User file created: User.json");
            }

        } catch (IOException e) {
            System.out.println("Error creating user file: " + e.getMessage());
        }
    }

    // Load bio for a specific speaker by username
    public static String loadSpeakerBio(String username) {
        try {
            File bioFile = new File("speaker.json");
            if (!bioFile.exists()) {
                return "No bio available";
            }

            List<String> lines = Files.readAllLines(Paths.get("speaker.json"));
            for (int i = 0; i < lines.size(); i += 2) {
                if (i + 1 < lines.size()) {
                    if (lines.get(i).equals(username)) {
                        return lines.get(i + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading speaker bio: " + e.getMessage());
        }
        return "No bio available";
    }

    // validation for signup
    public static boolean validationExist(String name, User[] existUser) {

        if (validationEmpty(name)) {

            return false;
        }

        if (existUser == null) {
            return false;
        }

        for (int i = 0; i < ems.getUser_No(); i++) {
            if (existUser[i] != null && name.strip().equals(existUser[i].getUsername())) {
                System.out.println("Error: The Username Has Already Exist ! ");
                return false;
            }
        }

        return true;
    }

    public static boolean validationName(String name) {
        char[] namearray = name.strip().toCharArray();
        if (validationEmpty(name)) {
            return false;

        } else if (namearray.length < 3) {
            System.out.println("Input Error : Your Name length must be at least 3 length.");
            return false;
        }

        else {

            for (char charname : namearray) {
                if (!((int) charname >= 65 && (int) charname <= 90)
                        && !((int) charname >= 97 && (int) charname <= 122)) {
                    System.out.println("Input Error : Please Enter In alphabet !");
                    return false;
                }
            }

            return true;
        }

    }

    public static boolean validationEmail(String email) {
        if (validationEmpty(email)) {
            return false;
        }
        char[] emailArray = email.toCharArray();

        // check the first character of email
        if (!((int) emailArray[0] >= 65 && (int) emailArray[0] <= 90)
                && !((int) emailArray[0] >= 97 && (int) emailArray[0] <= 122)) {
            System.out.println("Input Error: The first Character Cannot Be Symbols ! ");
            return false;
        }
        // check the email format
        else if (!(email.contains("@gmail.com"))) {

            System.out.println("Input Error: Please Input In Gmail Format ! ");
            return false;

        } else {

            return true;

        }
    }

    // validation for contact number
    public static boolean validationcontactNo(String contactNo) {

        char[] contactNoArray = contactNo.toCharArray();
        if (validationEmpty(contactNo)) {
            return false;
        }

        // check the contact number length
        else if (contactNoArray.length != 11 && contactNoArray.length != 10) {
            System.out.println("Input Error: Please Enter In Format ! ");
            return false;

        }
        // check the character is in number

        else {
            for (int i = 0; i < contactNoArray.length; i++) {
                if (!((int) contactNoArray[i] >= 48 && (int) contactNoArray[i] <= 57)) {
                    System.out.println("Input Error: Please Enter In Format ! ");
                    return false;
                }
            }

            return true;

        }

    }

    // validation password (it length must be more than 5 char)
    public static boolean validationPassword(String password) {
        if (validationEmpty(password)) {
            return false;
        }
        if (password.length() < 5) {

            System.out.println("Input Error: Your Password Must be More Than 5 Character ! ");

            return false;
        } else {
            return true;
        }
    }

    public static boolean validationPassword2(String password, String password2) {
        if (validationEmpty(password2)) {
            return false;
        }
        if (!(password2.equals(password))) {
            System.out.println(" Error: Your Password Is Not Matched ! ");

            return false;
        } else {
            return true;
        }
    }

    public static boolean validationEmpty(String data) {
        if (data == null) {
            System.out.println("Input Error: Don't Empty Your Input ! ");

            return true;

        } else if (data.isEmpty()) {
            System.out.println("Input Error: Don't Empty Your Input ! ");

            return true;

        } else {

            return false;
        }
    }

    // validation for login

    public static boolean validationNoExistName(User user, User[] existUser, int[] no) {
        // check the user input is empty or not
        if (validationEmpty(user.getUsername())) {
            return false;
        }
        for (int j = 0; j < ems.getUser_No(); j++) {
            if (existUser[j] != null && user.equals(existUser[j])) {
                no[0] = j;
                // found user
                return true;
            }
        }
        user.setUserName(null);
        System.out.println("Error: The Username Is not Matched ! ");

        return false;

    }

    public static boolean validationLoginPwd(String password, User[] user, int[] no) {
        if (validationEmpty(password)) {
            return false;
        }

        String current_pswd = user[no[0]].getPassword();
        if (password.equals(current_pswd)) {
            return true;

        }
        System.out.println("Error: Inccorrect Password ! ");

        return false;
    }

    // additional function
    public static void waitForEnter() {
        try {
            scan.nextLine(); // Reads a single byte
            System.in.skip(System.in.available()); // Clear buffer
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearScreen() {
        try {
            // Try ANSI escape codes first
            System.out.flush();
        } catch (Exception e) {
            // Fallback to blank lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    // _________________________________________________________________________
    // Staff Part
    // _________________________________________________________________________
    public static void staffMenu(Staff staff, User[] alluser, int[] no) {
        boolean inMenu = true;
        clearScreen();

        try {

            while (inMenu) {
                System.out.println("\n--------------------------------------------------------------");
                System.out.println("|                      STAFF MENU                            |");
                System.out.println("-------------------------------------------------------------|");
                System.out.println("|       CHECK-IN MENU:                                       |");
                System.out.println("|     1. Check-in Attendee                                   |");
                System.out.println("|     2. View Check-in List                                  |");
                System.out.println("|     3. View Pending Attendees List                         |");
                System.out.println("-------------------------------------------------------------|");
                System.out.println("|       REPORT MENU:                                         |");
                System.out.println("|     4. View Event Report                                   |");
                System.out.println("|     5. View Sales Report                                   |");
                System.out.println("|     6. View All Check-ins Report                           |");
                System.out.println("-------------------------------------------------------------|");
                System.out.println("|        EXPORT MENU:                                        |");
                System.out.println("|     7. Export Check-in Report                              |");
                System.out.println("|     8. Export Sales Report                                 |");
                System.out.println("|     9. Export Event Report                                 |");
                System.out.println("-------------------------------------------------------------|");
                System.out.println("|     0. Exit                                                |");
                System.out.println("--------------------------------------------------------------");
                System.out.print("Enter option: ");

                int choice = scan.nextInt();

                switch (choice) {
                    case 1:
                        checkIn_Attendee(alluser, no);
                        break;
                    case 2:
                        view_checkin(alluser);
                        break;
                    case 3:
                        view_pending_attendee_list(alluser);
                        break;
                    case 4:
                        event_report();
                        break;
                    case 5:
                        sale_report();
                        break;
                    case 6:
                        all_check_in_report(alluser);
                        break;
                    case 7:
                        exportCheckInReportToFile(alluser);
                        break;
                    case 8:
                        exportSalesReportToFile();
                        break;
                    case 9:
                        exportEventReportToFile();
                        break;
                    case 0:
                        inMenu = false;
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }

            }
        } catch (Exception e) {
            System.out.println("Error: Existing Unknown Char. Please Input Correctly!!!");
            scan.nextLine();
            inMenu = true;
            waitForEnter();
        } finally {
            if (inMenu) {
                staffMenu(staff, alluser, no);

            }

        }
    }

    public static void checkIn_Attendee(User[] alluser, int[] no) {
        User current_attendee = null;
        Ticket currentTicket = null;
        boolean search = true;

        clearScreen();
        System.out.println("\n\t\t--------------------------------------------------------------");
        System.out.println("\t\t|                   CHECK-IN ATTENDEE                        |");
        System.out.println("\t\t--------------------------------------------------------------\n\n");
        System.out.println("\t\t        1. Ticket ID       2. Booking ID      0. Exit");

        int option = 0;

        // Get valid option
        try {
            do {

                System.out.print("\n\t\tEnter Your Option: ");
                option = scan.nextInt();
                scan.nextLine();

            } while (option > 2 || option < 0);
        } catch (Exception e) {
            System.out.println("\t\tError: Invalid Character!!! Please enter number.");
            scan.nextLine(); // Clear buffer
            checkIn_Attendee(alluser, no);
            return;
        }

        if (option == 0) {
            return; // Exit
        }
        Attendee usertemp = new Attendee();

        // Process based on option
        switch (option) {
            case 1:
                System.out.print("\n\t\tEnter Ticket ID: ");
                String ticketId = scan.nextLine();
                boolean ticketFound = false;
                for (Ticket ticket : tickets) {

                    usertemp.setUserName(ticket.getBuyerName());
                    if (ticket.hasTicket(ticketId)) {
                        ticketFound = true;

                        // Find attendee by buyer name
                        for (int i = 0; i < ems.getUser_No(); i++) {
                            if (alluser[i].equals(usertemp)) {
                                current_attendee = alluser[i];
                                currentTicket = ticket;
                                search = false;
                                break;
                            }
                        }

                        if (current_attendee == null) {
                            System.out.println("\t\tERROR: Attendee Information not Found!");
                            waitForEnter();
                            checkIn_Attendee(alluser, no);
                            return;
                        }
                        break;
                    }
                }

                if (!ticketFound) {
                    System.out.println("\t\tERROR: Ticket ID not Found!");
                    System.out.println("Please Enter Key To Continue...");
                    waitForEnter();
                    checkIn_Attendee(alluser, no);
                    return;
                }
                break;

            case 2:
                System.out.print("\n\t\tEnter Booking ID: ");
                String bookingId = scan.nextLine();
                boolean bookingFound = false;

                for (Ticket ticket : tickets) {
                    if (ticket.getBookingId().equals(bookingId)) {

                        bookingFound = true;
                        currentTicket = ticket;
                        usertemp.setUserName(ticket.getBuyerName());
                        // Find attendee by buyer name
                        for (int i = 0; i <ems.getUser_No(); i++) {
                            if (alluser[i].equals(usertemp)) {
                                current_attendee = alluser[i];
                                search = false;
                                break;
                            }
                        }

                        if (current_attendee == null) {

                            System.out.println("\t\tERROR: Attendee Information not Found!");
                            System.out.println("Please Enter Key To Continue...");
                            waitForEnter();
                            checkIn_Attendee(alluser, no);
                            return;
                        }
                        break;
                    }
                }

                if (!bookingFound) {
                    System.out.println("\t\tERROR: Booking ID not Found!");
                    System.out.println("Please Enter Key To Continue...");
                    waitForEnter();
                    checkIn_Attendee(alluser, no);
                    return;
                }
                break;
            default :
                checkIn_Attendee(alluser, no);
                return;
        }

        // Check if already checked in
        if (!currentTicket.getStatus()) {
            System.out.println("\n\t\t--------------------------------------------------------------");
            System.out.println("\t\t|                    ALREADY CHECKED IN!                     |");
            System.out.println("\t\t--------------------------------------------------------------");
            System.out.println("Please Enter Key To Continue...");
            waitForEnter();
            checkIn_Attendee(alluser, no);
            return;
        }

        System.out.println(
                "-------------------------------------------------------------\n" +
                        "|                      CHECK-IN DETAILS                     |\n" +
                        "------------------------------------------------------------|\n" +
                        current_attendee.toString() + currentTicket.toString() +
                        "-------------------------------------------------------------");

        System.out.println("\n     Press Enter Key To Continue");
        waitForEnter();
        System.out.println("\n\t\t--------------------------------------------------------------");
        System.out.println("\t\t|                   CONFIRM CHECK-IN                         |");
        System.out.println("\t\t--------------------------------------------------------------\n\n");
        System.out.println("\t\t          1. Yes                        0. No");

        try {
            do {
                System.out.print("\n\t\tEnter Your Option: ");
                option = scan.nextInt();
                scan.nextLine();
            } while (option > 2 || option < 0);
        } catch (Exception e) {
            System.out.println("\t\tError: Invalid Character!!! Please enter number.");
            scan.nextLine(); // Clear buffer
            checkIn_Attendee(alluser, no);
            return;
        }

        if (option == 1) {
            currentTicket.setStatus(false);
            Staff.increase_total_Checkin_counter();
            checkIn_Attendee(alluser, no); // Exit
        } else {
            return;
        }

    }

    public static void view_checkin(User[] alluser) {
        int no = 1;

        System.out.println("\n\t\t----------------------------------------------------------------------------------");
        System.out.println("\t\t|                           CHECK-IN LIST                                        |");
        System.out.printf("\t\t|                               %-48s |\n", LocalDate.now());
        System.out.println("\t\t----------------------------------------------------------------------------------");
        if (Staff.getTotal_Checkin_counter() == 0) {
            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                            NO CHECK-INS TODAY                                  |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
            return;
        } else {
            System.out.println("\n\t\t\t----------------------------------------------------------------------");
            System.out.println("\t\t\t|                            CHECKED-IN LIST                         |");
            System.out.println("\t\t\t|--------------------------------------------------------------------|");
            System.out.println("\t\t\t| NO |      Name          |        Email        |  Ticket  | Status  |");
            System.out.println("\t\t\t|----|--------------------|---------------------|----------|---------|");
            Attendee usertemp = new Attendee();
            for (Ticket ticket : tickets) {
                if (ticket != null) {

                    usertemp.setUserName(ticket.getBuyerName());

                    if (alluser.length == 0) {
                        for (User user : alluser) {
                            if (!(user.equals(usertemp))) {
                                System.out.println("ERROR: No Any Attendee Resgister The System !!!");
                                view_pending_attendee_list(alluser);
                                break;
                            }
                        }
                        System.out.println("ERROR: No Any Attendee Resgister The System !!!");
                        return;
                    }
                    if (!ticket.getStatus()) {
                        for (User user : alluser) {
                            if (!(user.equals(usertemp))) {
                                continue;
                            }
                            if (user.hasUser(ticket.getBuyerName())) {
                                System.out.printf("\t\t\t| %-2d | %-18s | %-19s | %-8s | %-8s|\n",
                                        no++,
                                        ticket.getBuyerName(),
                                        user.getEmail(),
                                        ticket.getTicketId(),
                                        status_ToString(ticket.getStatus()));
                                break;
                            }

                        }
                    }

                }

            }
            System.out.println("\t\t\t----------------------------------------------------------------------");
            System.out.println("\n\n\t\t\t Press Enter Key To Return Menu");
            waitForEnter();
            waitForEnter();

        }

    }

    public static void view_pending_attendee_list(User[] alluser) {
        System.out.println("\n\t\t----------------------------------------------------------------------------------");
        System.out.println("\t\t|                           PENDING ATTENDEES LIST                               |");
        System.out.printf("\t\t|                               %-48s |\n", LocalDate.now());
        System.out.println("\t\t----------------------------------------------------------------------------------");
        if (tickets.size() == 0) {
            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                  NO ANY Buyer                                  |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
            return;
        } else {
            System.out.println("\n\t\t\t----------------------------------------------------------------------");
            System.out.println("\t\t\t|                     PENDING ATTENDEES LIST                         |");
            System.out.println("\t\t\t|--------------------------------------------------------------------|");
            System.out.println("\t\t\t| No |      Name          |        Email        |  Ticket  | Status  |");

            int no = 1;
            Attendee usertemp = new Attendee();
            for (Ticket ticket : tickets) {
                if (ticket != null) {

                    if (alluser.length == 0) {
                        for (User user : alluser) {
                            if (!(usertemp.checkClass(user))) {
                                System.out.println("ERROR: NO Any Attendee Resgister The System !!!");
                                view_pending_attendee_list(alluser);
                                break;
                            }
                        }
                        System.out.println("ERROR: NO Any Attendee Resgister The System !!!");
                        return;
                    }
                    if (ticket.getStatus()) {
                        for (User user : alluser) {
                            if (!(usertemp.checkClass(user))) {
                                continue;
                            }
                            if (user.hasUser(ticket.getBuyerName())) {
                                System.out.println(
                                        "\t\t\t|----|--------------------|---------------------|----------|---------|");
                                System.out.printf("\t\t\t| %-2d | %-18s | %-19s | %-8s | %-8s|\n",
                                        no++,
                                        ticket.getBuyerName(),
                                        user.getEmail(),
                                        ticket.getTicketId(),
                                        status_ToString(ticket.getStatus()));
                                break;
                            }

                        }
                    }

                }
            }
            System.out.println("\t\t\t----------------------------------------------------------------------");
            System.out.println("\n\n\t\t\t\t Press Enter Key To Return Menu");
            waitForEnter();
            waitForEnter();
        }

    }

    public static void all_check_in_report(User[] alluser) {
        System.out.println("\n\t\t----------------------------------------------------------------------------------");
        System.out.println("\t\t|                              ALL CHECK-INS REPORT                              |");
        System.out.printf("\t\t|                              Generated :%-38s |\n", LocalDate.now());
        System.out.printf("\t\t|                               Total Check-ins : %-30s |\n", tickets.size());
        System.out.println("\t\t----------------------------------------------------------------------------------");
        if (tickets.size() == 0) {
            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                  NO ANY BUYER                                  |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
            return;
        } else {
            System.out.println("\n\t\t\t----------------------------------------------------------------------");
            System.out.println("\t\t\t|                       ALL CHECK-INS REPORT                         |");
            System.out.println("\t\t\t|--------------------------------------------------------------------|");
            System.out.println("\t\t\t| NO |      Name          |        Email        |  Ticket  | Status  |");
            int no = 1;

            Attendee usertamp = new Attendee();

            for (Ticket ticket : tickets) {
                if (ticket != null) {

                    usertamp.setUserName(ticket.getBuyerName());

                    if (alluser.length == 0) {
                        for (User user : alluser) {
                            if (!(usertamp.equals(user))) {
                                System.out.println("ERROR: NO Any Attendee Resgister The System !!!");
                                view_pending_attendee_list(alluser);
                                break;
                            }
                        }
                        System.out.println("ERROR: NO Any Attendee Resgister The System !!!");
                        return;
                    }
                    for (User user : alluser) {
                        if (!(usertamp.checkClass(user))) {
                            continue;
                        }
                        if (user.hasUser(ticket.getBuyerName())) {
                            System.out.println(
                                    "\t\t\t|----|--------------------|---------------------|----------|---------|");
                            System.out.printf("\t\t\t| %-2d | %-18s | %-19s | %-8s | %-8s|\n",
                                    no++,
                                    ticket.getBuyerName(),
                                    user.getEmail(),
                                    ticket.getTicketId(),
                                    status_ToString(ticket.getStatus()));
                            break;
                        }

                    }

                }
            }

            System.out.println("\t\t\t----------------------------------------------------------------------");
            System.out.println("\n\n\t\t\t\t Press Enter Key To Mone On To Next Page");
            waitForEnter();
            waitForEnter();

            int checkinRate = (Staff.getTotal_Checkin_counter() * 100) / tickets.size();
            String rateText = checkinRate + "%";
            System.out.println("\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println("\t\t\t|                                 CHECK-IN STATISTICS                            |");
            System.out.println("\t\t\t----------------------------------------------------------------------------------");
            System.out.println("\t\t\t|                                                                                |");
            System.out.println("\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Total Check-ins          :             %-32d |\n",
                    Staff.getTotal_Checkin_counter());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Total Attendees          :             %-32d |\n", tickets.size());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Check-in Rate            :             %-32s |\n", rateText);
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t|                                 Check-in Rate                                  |");
            System.out.println(
                    "\t\t\t|            ----------------------------------------------------------------    |");
            System.out.print("\t\t\t|            | ");
            int displaybar = (int) ((double) (checkinRate * 49) / 100);
            for (int i = 0; i < 49; i++) {
                if (i <= displaybar) {
                    System.out.print("||");
                } else {
                    System.out.print(" ");
                }

            }

            System.out.println("  |    |");
            System.out.println(
                    "\t\t\t|            ----------------------------------------------------------------    |");
            System.out.println(
                    "\t\t\t|                                                                                | ");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");

            System.out.println("\t\t\tPlease Press Enter Key to Return Back Menu.");
            waitForEnter();

        }

    }

    public static void event_report() {
        System.out.println("\n\t\t----------------------------------------------------------------------------------");
        System.out.println("\t\t|                                    EVENT REPORT                                |");
        System.out.printf("\t\t|                              Generated    :%-35s |\n", LocalDate.now());
        System.out.println("\t\t----------------------------------------------------------------------------------");

        System.out
                .println("\n\t\t\t-----------------------------------------------------------------------------------");
        System.out.println("\t\t\t|                              Event List                                         |");
        System.out.println("\t\t\t|---------------------------------------------------------------------------------|");
        System.out.println("\t\t\t| Event ID   |      Title         |        Venue      |    Date    |      Type    |");

        if (events.length == 0) {

            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                  No ANY EVENT                                  |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");

            System.out.println("Press Enter Key to Return Back ...");
            waitForEnter();
            return;
        } else {
            for (Event event : events) {
                if (event != null) {

                    System.out.print(
                            "\t\t\t|------------|--------------------|-------------------|------------|--------------|\n"
                                    +
                                    event.toString());
                }
            }
        }

        System.out.println(
                "\t\t\t-----------------------------------------------------------------------------------");

        System.out.print("\n\t\t\tEnter Event ID : ");

        scan.nextLine();
        String eventId = scan.nextLine();

        Event current_event = ems.findEventById(eventId);
        if (current_event != null) {
            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                 EVENT INFORMATION                              |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Event ID                 :             %-32s |\n",
                    current_event.getEventID());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Event Name               :             %-32s |\n",
                    current_event.getTitle());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Date                     :             %-32s |\n",
                    current_event.getDate());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Venue                    :             %-32s |\n",
                    current_event.getVenue());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Max Capacity             :             %-32d |\n",
                    current_event.getMaxTickets());
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");

            System.out.println("\t\t\tPlease Press Enter Key to Continue.");
            waitForEnter();

            TicketType current_TicketType = TicketType.findTicketTypeById(ticketTypes, eventId);
            int[] tol = current_TicketType.getQuantityOfAllTicketType();
            // Early Bird
            int totalEarly = tol[0];
            int soldEarly = totalEarly - current_TicketType.getAvailableType("earlybird");
            int availableEarly = current_TicketType.getAvailableType("earlybird");
            double revenueEarly = soldEarly * current_TicketType.getPrice("earlybird");

            // Standard
            int totalStandard = tol[1];
            int soldStandard = totalStandard - current_TicketType.getAvailableType("standard");
            int availableStandard = current_TicketType.getAvailableType("standard");
            double revenueStandard = soldStandard * current_TicketType.getPrice("standard");

            // VIP
            int totalVip = tol[2];
            int soldVip = totalVip - current_TicketType.getAvailableType("vip");
            int availableVip = current_TicketType.getAvailableType("vip");
            double revenueVip = soldVip * current_TicketType.getPrice("vip");

            // Print table
            System.out.println(
                    "\n\t\t\t-----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                 Tickets Details                                 |");
            System.out.println(
                    "\t\t\t|---------------------------------------------------------------------------------|");
            System.out.println(
                    "\t\t\t| Ticket Type        | Total        | Sold         | Available    | Revenue       |");
            System.out.println(
                    "\t\t\t|--------------------|--------------|--------------|--------------|---------------|");

            System.out.printf("\t\t\t| Early Bird         | %-12d | %-12d | %-12d | RM %-10.2f |\n",
                    totalEarly, soldEarly, availableEarly, revenueEarly);

            System.out.printf("\t\t\t| Standard           | %-12d | %-12d | %-12d | RM %-10.2f |\n",
                    totalStandard, soldStandard, availableStandard, revenueStandard);

            System.out.printf("\t\t\t| VIP                | %-12d | %-12d | %-12d | RM %-10.2f |\n",
                    totalVip, soldVip, availableVip, revenueVip);

            System.out.println(
                    "\t\t\t|--------------------|--------------|--------------|--------------|---------------|");

            int totalAll = totalEarly + totalStandard + totalVip;
            int soldAll = soldEarly + soldStandard + soldVip;
            int availableAll = availableEarly + availableStandard + availableVip;
            double revenueAll = revenueEarly + revenueStandard + revenueVip;

            System.out.printf("\t\t\t| TOTAL              | %-12d | %-12d | %-12d | RM %-10.2f |\n",
                    totalAll, soldAll, availableAll, revenueAll);
            System.out.println(
                    "\t\t\t-----------------------------------------------------------------------------------");

            waitForEnter();
            System.out.println("\t\t\tPlease Press Enter Key to Continue.");

            int total_ticket_checkin = 0;
            for (Ticket ticket : tickets) {
                if (ticket.getEventId().equals(eventId)) {
                    if (!ticket.getStatus()) {
                        total_ticket_checkin++;

                    }
                }
            }

            double checkinRate = (soldAll > 0) ? (double) total_ticket_checkin / soldAll * 100 : 0;
            String rateText = checkinRate + "%";
            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                 CHECK-IN STATISTICS                            |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Total Check-ins          :             %-32d |\n",
                    total_ticket_checkin);
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Total Attendees          :             %-32d |\n", soldAll);
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.printf("\t\t\t|        Check-in Rate            :             %-32s |\n", rateText);
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t|                                                                                |");
            System.out.println(
                    "\t\t\t|                                 Check-in Rate                                  |");
            System.out.println(
                    "\t\t\t|            ----------------------------------------------------------------    |");
            System.out.print("\t\t\t|            | ");

            int displaybar = (int) ((double) checkinRate / 100 * 49);

            for (int i = 0; i < 49; i++) {
                if (displaybar >= i) {
                    System.out.print("||");
                } else {
                    System.out.print(" ");
                }

            }

            System.out.println("  |    |");
            System.out.println(
                    "\t\t\t|            ----------------------------------------------------------------    |");
            System.out.println(
                    "\t\t\t|                                                                                | ");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");

            waitForEnter();
            waitForEnter();
            System.out.println("\t\t\tPlease Press Enter Key to Return Back Menu.");

        } else {
            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                  NO FOUND                                      |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
        }
    }

    public static void sale_report() {

        System.out.println("\n\t\t----------------------------------------------------------------------------------");
        System.out.println("\t\t|                                  SALES REPORT                                   |");
        System.out.printf("\t\t|                              Generated    :%-35s |\n", LocalDate.now());
        System.out.println("\t\t----------------------------------------------------------------------------------");

        System.out
                .println("\n\t\t\t-----------------------------------------------------------------------------------");
        System.out.println("\t\t\t|                              Event List                                         |");
        System.out.println("\t\t\t|---------------------------------------------------------------------------------|");
        System.out.println("\t\t\t| Event ID   |      Title         |        Venue      |    Date    |      Type    |");

        if (events.length == 0) {

            System.out.println(
                    "\n\t\t\t----------------------------------------------------------------------------------");
            System.out.println(
                    "\t\t\t|                                  NO ANY EVENT                                  |");
            System.out.println(
                    "\t\t\t----------------------------------------------------------------------------------");
        } else {
            for (Event event : events) {
                if (event != null) {
                    System.out.print(
                            "\t\t\t|------------|--------------------|-------------------|------------|--------------|\n"
                                    +
                                    event.toString());
                }

            }

            System.out.println(
                    "\t\t\t-----------------------------------------------------------------------------------");

            System.out.print("\n\t\t\tEnter Event ID : ");

            scan.nextLine();
            String eventId = scan.nextLine();

            Event current_event = ems.findEventById(eventId);
            if (current_event != null) {
                System.out.println(
                        "\n\t\t\t----------------------------------------------------------------------------------");
                System.out.println(
                        "\t\t\t|                                 EVENT INFORMATION                              |");
                System.out.println(
                        "\t\t\t----------------------------------------------------------------------------------");
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.printf("\t\t\t|        Event ID                 :             %-32s |\n",
                        current_event.getEventID());
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.printf("\t\t\t|        Event Name               :             %-32s |\n",
                        current_event.getTitle());
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.printf("\t\t\t|        Date                     :             %-32s |\n",
                        current_event.getDate());
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.printf("\t\t\t|        Venue                    :             %-32s |\n",
                        current_event.getVenue());
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.printf("\t\t\t|        Max Capacity             :             %-32d |\n",
                        current_event.getMaxTickets());
                System.out.println(
                        "\t\t\t|                                                                                |");
                System.out.println(
                        "\t\t\t----------------------------------------------------------------------------------");

                System.out.println("\t\t\tPlease Press Enter Key to Continue.");
                waitForEnter();

                TicketType current_TicketType = TicketType.findTicketTypeById(ticketTypes, eventId);
                int[] tol = current_TicketType.getQuantityOfAllTicketType();
                // Early Bird
                double totalEarly = (double) tol[0] * current_TicketType.getPrice("earlybird");
                double soldEarly = (totalEarly > 0)
                        ? totalEarly - current_TicketType.getAvailableType("earlybird")
                                * current_TicketType.getPrice("earlybird")
                        : 0;
                double availableEarly = (double) current_TicketType.getAvailableType("earlybird")
                        * current_TicketType.getPrice("earlybird");
                double revenueEarly = soldEarly;

                // Standard
                double totalStandard = (double) tol[1] * current_TicketType.getPrice("standard");
                double soldStandard = (totalStandard > 0)
                        ? totalStandard - (current_TicketType.getAvailableType("standard"))
                                * current_TicketType.getPrice("standard")
                        : 0;
                double availableStandard = current_TicketType.getAvailableType("standard")
                        * current_TicketType.getPrice("standard");
                double revenueStandard = soldStandard;

                // VIP
                double totalVip = (double) tol[2] * current_TicketType.getPrice("vip");
                double soldVip = (totalVip > 0)
                        ? totalVip - current_TicketType.getAvailableType("vip") * current_TicketType.getPrice("vip")
                        : 0;
                double availableVip = current_TicketType.getAvailableType("vip") * current_TicketType.getPrice("vip");
                double revenueVip = soldVip;

                // Print table
                System.out.println(
                        "\n\t\t\t-----------------------------------------------------------------------------------");
                System.out.println(
                        "\t\t\t|                                 Tickets Sales                                   |");
                System.out.println(
                        "\t\t\t|---------------------------------------------------------------------------------|");
                System.out.println(
                        "\t\t\t| Ticket Type        | Total        | Sold         | Available    | Revenue       |");
                System.out.println(
                        "\t\t\t|--------------------|--------------|--------------|--------------|---------------|");

                System.out.printf("\t\t\t| Early Bird         | RM %-9.2f | RM %-9.2f | RM %-9.2f | RM %-10.2f |\n",
                        totalEarly, soldEarly, availableEarly, revenueEarly);

                System.out.printf("\t\t\t| Standard           | RM %-9.2f | RM %-9.2f | RM %-9.2f | RM %-10.2f |\n",
                        totalStandard, soldStandard, availableStandard, revenueStandard);

                System.out.printf("\t\t\t| VIP                | RM %-9.2f | RM %-9.2f | RM %-9.2f | RM %-10.2f |\n",
                        totalVip, soldVip, availableVip, revenueVip);

                System.out.println(
                        "\t\t\t|--------------------|--------------|--------------|--------------|---------------|");

                double totalAll = totalEarly + totalStandard + totalVip;
                double soldAll = soldEarly + soldStandard + soldVip;
                double availableAll = availableEarly + availableStandard + availableVip;
                double revenueAll = revenueEarly + revenueStandard + revenueVip;

                System.out.printf("\t\t\t| TOTAL              | RM %-9.2f | RM %-9.2f | RM %-9.2f | RM %-10.2f |\n",
                        totalAll, soldAll, availableAll, revenueAll);
                System.out.println(
                        "\t\t\t-----------------------------------------------------------------------------------");

                waitForEnter();
                System.out.println("\t\t\tPlease Press Enter Key to Continue.");

            } else {
                System.out.println(
                        "\n\t\t\t----------------------------------------------------------------------------------");
                System.out.println(
                        "\t\t\t|                                  NO FOUND                                      |");
                System.out.println(
                        "\t\t\t----------------------------------------------------------------------------------");
            }

        }

    }

    public static String status_ToString(boolean status) {
        if (status) {
            return "Pending";
        } else {
            return "Confirm";
        }
    }

    public static void exportCheckInReportToFile(User[] alluser) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "CheckIn_Report_" + timestamp + ".txt";
            FileWriter writer = new FileWriter(filename);

            writer.write("\t\t----------------------------------------------------------------------------------\n");
            writer.write("\t\t|                           CHECK-IN REPORT                                      |\n");
            writer.write("\t\t---------------------------------------------------------------------------------|\n");
            writer.write("\t\t|  Generated: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "                                      |\n");
            writer.write("\t\t---------------------------------------------------------------------------------|\n");
            writer.write("\t\t|                                                                                |\n");
            writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            writer.write("\t\t|  |                          CHECK-IN HISTORY                                | |\n");
            writer.write("\t\t|  |--------------------------------------------------------------------------| |\n");
            writer.write("\t\t|  | NO | Name               | Email               | Ticket ID    | Status    | |\n");
            writer.write("\t\t|  |----|--------------------|---------------------|--------------|-----------| |\n");
            Attendee usertemp = new Attendee();
            int no = 1;
            for (Ticket ticket : tickets) {
                usertemp.setUserName(ticket.getBuyerName());
                if (ticket != null) {
                    for (User user : alluser) {
                        if (user.equals(usertemp)) {
                            writer.write(String.format("\t\t|  %2d | %-18s | %-19s | %-12s | %-8s | |\n",
                                    no++, ticket.getBuyerName(), user.getEmail(),
                                    ticket.getTicketId(), status_ToString(ticket.getStatus())));
                            break;
                        }
                    }
                }
            }

            writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            writer.write("\t\t|                                                                                |\n");
            writer.write("\t\t----------------------------------------------------------------------------------\n");

            writer.close();
            System.out.println("\n\t\t Check-in Report exported successfully!");
            System.out.println("\t\t   File: " + filename);
        } catch (IOException e) {
            System.out.println("\t\t Error exporting check-in report: " + e.getMessage());
        }
        waitForEnter();
    }

    public static void exportSalesReportToFile() {

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Sales_Report_" + timestamp + ".txt";
            FileWriter writer = new FileWriter(filename);

            writer.write("\t\t----------------------------------------------------------------------------------\n");
            writer.write("\t\t|                                SALES REPORT                                   |\n");
            writer.write("\t\t---------------------------------------------------------------------------------|\n");
            writer.write("\t\t|  Generated: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "                                      |\n");
            writer.write("\t\t---------------------------------------------------------------------------------|\n");
            writer.write("\t\t|                                                                                |\n");
            writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            writer.write("\t\t|  |                           REVENUE BREAKDOWN                               | |\n");
            writer.write("\t\t|  |---------------------------------------------------------------------------| |\n");
            writer.write("\t\t|  | Ticket Type        | Sold         | Revenue                               | |\n");
            writer.write("\t\t|  |--------------------|------------------------------------------------------| |\n");

            // Calculate totals
            int earlyBirdSold = 0, standardSold = 0, vipSold = 0;
            double earlyBirdRevenue = 0, standardRevenue = 0, vipRevenue = 0;

            for (Ticket ticket : tickets) {
                if (ticket != null) {
                    switch (ticket.getTicketType().toLowerCase()) {
                        case "earlybird":
                            earlyBirdSold++;
                            earlyBirdRevenue += ticket.getTotalAmount();
                            break;
                        case "standard":
                            standardSold++;
                            standardRevenue += ticket.getTotalAmount();
                            break;
                        case "vip":
                            vipSold++;
                            vipRevenue += ticket.getTotalAmount();
                            break;
                    }
                }
            }

            writer.write(String.format("\t\t|  | Early Bird         | %-12d | RM %-40.2f |\n", earlyBirdSold,
                    earlyBirdRevenue));
            writer.write(String.format("\t\t|  | Standard           | %-12d | RM %-40.2f |\n", standardSold,
                    standardRevenue));
            writer.write(String.format("\t\t|  | VIP                | %-12d | RM %-40.2f |\n", vipSold, vipRevenue));

            double totalRevenue = earlyBirdRevenue + standardRevenue + vipRevenue;
            int totalSold = earlyBirdSold + standardSold + vipSold;

            writer.write("\t\t|  |--------------------|------------------------------------------------------| |\n");
            writer.write(
                    String.format("\t\t|  | TOTAL              | %-12d | RM %-40.2f |\n", totalSold, totalRevenue));
            writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            writer.write("\t\t|                                                                                |\n");
            writer.write("\t\t----------------------------------------------------------------------------------\n");

            writer.close();
            System.out.println("\n\t\t Sales Report exported successfully!");
            System.out.println("\t\t   File: " + filename);
        } catch (IOException e) {
            System.out.println("\t\t Error exporting sales report: " + e.getMessage());
        }
        waitForEnter();
    }

    public static void exportEventReportToFile() {
        scan.nextLine();
        System.out.print("\n\t\tEnter Event ID to export: ");
        String eventId = scan.nextLine();

        Event current_event = ems.findEventById(eventId);
        if (current_event == null) {
            System.out.println("\t\t Event not found!");
            waitForEnter();
            return;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Event_Report_" + eventId + "_" + timestamp + ".txt";
            FileWriter writer = new FileWriter(filename);

            writer.write("\t\t----------------------------------------------------------------------------------\n");
            writer.write("\t\t|                                EVENT REPORT                                    |\n");
            writer.write("\t\t---------------------------------------------------------------------------------|\n");
            writer.write("\t\t|                                                                                |\n");
            writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            writer.write("\t\t|  |                           EVENT INFORMATION                              | |\n");
            writer.write("\t\t|  |--------------------------------------------------------------------------| |\n");
            writer.write(String.format("\t\t|  |  Event ID       : %-62s |\n", current_event.getEventID()));
            writer.write(String.format("\t\t|  |  Event Name     : %-62s |\n", current_event.getTitle()));
            writer.write(String.format("\t\t|  |  Date           : %-62s |\n", current_event.getDate()));
            writer.write(String.format("\t\t|  |  Venue          : %-62s |\n", current_event.getVenue()));
            writer.write(String.format("\t\t|  |  Max Capacity   : %-62d |\n", current_event.getMaxTickets()));
            writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            writer.write("\t\t|                                                                                |\n");

            TicketType current_TicketType = TicketType.findTicketTypeById(ticketTypes, eventId);
            if (ticketTypes != null) {
                writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
                writer.write("\t\t|  |                           TICKET SALES                                   | |\n");
                writer.write("\t\t|  |--------------------------------------------------------------------------| |\n");
                writer.write("\t\t|  | Ticket Type        | Total        | Sold         | Revenue               | |\n");
                writer.write("\t\t|  |--------------------|--------------|--------------|-----------------------| |\n");

                int[] tol = current_TicketType.getQuantityOfAllTicketType();
                // Early Bird
                int totalEarly = tol[0];
                int soldEarly = totalEarly - current_TicketType.getAvailableType("earlybird");
                double revenueEarly = soldEarly * current_TicketType.getPrice("earlybird");

                // Standard
                int totalStandard = tol[1];
                int soldStandard = totalStandard - current_TicketType.getAvailableType("standard");
                double revenueStandard = soldStandard * current_TicketType.getPrice("standard");

                // VIP
                int totalVip = tol[2];
                int soldVip = totalVip - current_TicketType.getAvailableType("vip");
                double revenueVip = soldVip * current_TicketType.getPrice("vip");

                writer.write(String.format("\t\t|  | Early Bird         | %-12d | %-12d | RM %-22.2f |\n", totalEarly,
                        soldEarly, revenueEarly));
                writer.write(String.format("\t\t|  | Standard           | %-12d | %-12d | RM %-22.2f |\n",
                        totalStandard, soldStandard, revenueStandard));
                writer.write(String.format("\t\t|  | VIP                | %-12d | %-12d | RM %-22.2f |\n", totalVip,
                        soldVip, revenueVip));

                writer.write("\t\t|  |--------------------|--------------|--------------|-----------------------| |\n");
                writer.write(String.format("\t\t|  | TOTAL              | %-12d | %-12d | RM %-22.2f |\n",
                        (totalEarly + totalStandard + totalVip),
                        (soldEarly + soldStandard + soldVip),
                        (revenueEarly + revenueStandard + revenueVip)));
                writer.write("\t\t|  ---------------------------------------------------------------------------- |\n");
            }

            writer.write("\t\t|                                                                                |\n");
            writer.write("\t\t----------------------------------------------------------------------------------\n");

            writer.close();
            System.out.println("\n\t\t Event Report exported successfully!");
            System.out.println("\t\t   File: " + filename);
        } catch (IOException e) {
            System.out.println("\t\t Error exporting event report: " + e.getMessage());
        }
        waitForEnter();
    }

    public static void exportAllReportsToCSV() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // Export Check-in CSV
            String checkinFilename = "CheckIn_Report_" + timestamp + ".csv";
            FileWriter checkinWriter = new FileWriter(checkinFilename);
            checkinWriter.write("Name,Email,Ticket ID,Status\n");

            for (Ticket ticket : tickets) {
                if (ticket != null) {
                    checkinWriter.write(String.format("%s,%s,%s,%s\n",
                            ticket.getBuyerName(), "", ticket.getTicketId(), status_ToString(ticket.getStatus())));
                }
            }
            checkinWriter.close();

            // Export Sales CSV
            String salesFilename = "Sales_Report_" + timestamp + ".csv";
            FileWriter salesWriter = new FileWriter(salesFilename);
            salesWriter.write("Event ID,Event Name,Ticket Type,Sold,Revenue\n");

            int earlyBirdSold = 0, standardSold = 0, vipSold = 0;
            double earlyBirdRevenue = 0, standardRevenue = 0, vipRevenue = 0;

            for (Ticket ticket : tickets) {
                if (ticket != null) {
                    switch (ticket.getTicketType().toLowerCase()) {
                        case "earlybird":
                            earlyBirdSold++;
                            earlyBirdRevenue += ticket.getTotalAmount();
                            break;
                        case "standard":
                            standardSold++;
                            standardRevenue += ticket.getTotalAmount();
                            break;
                        case "vip":
                            vipSold++;
                            vipRevenue += ticket.getTotalAmount();
                            break;
                    }
                }
            }

            salesWriter.write(String.format("ALL,ALL,Early Bird,%d,%.2f\n", earlyBirdSold, earlyBirdRevenue));
            salesWriter.write(String.format("ALL,ALL,Standard,%d,%.2f\n", standardSold, standardRevenue));
            salesWriter.write(String.format("ALL,ALL,VIP,%d,%.2f\n", vipSold, vipRevenue));
            salesWriter.close();

            // Export Event CSV
            String eventFilename = "Event_Report_" + timestamp + ".csv";
            FileWriter eventWriter = new FileWriter(eventFilename);
            eventWriter.write("Event ID,Event Name,Date,Venue,Max Capacity\n");

            for (Event event : events) {
                if (event != null) {
                    eventWriter.write(String.format("%s,%s,%s,%s,%d\n",
                            event.getEventID(), event.getTitle(), event.getDate(),
                            event.getVenue(), event.getMaxTickets()));
                }
            }

            eventWriter.close();

            System.out.println("\n\t\t All Reports exported successfully!");
            System.out.println("\t\t   Check-in Report: " + checkinFilename);
            System.out.println("\t\t   Sales Report: " + salesFilename);
            System.out.println("\t\t   Event Report: " + eventFilename);
        } catch (IOException e) {
            System.out.println("\t\t Error exporting reports: " + e.getMessage());
        }
        waitForEnter();
    }

    // -------------------------------------------------------------------------
    // ORGANIZER MENU
    // -------------------------------------------------------------------------
    static void organizerMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--------------------------------");
            System.out.println("|      ORGANIZER MENU          |");
            System.out.println("|------------------------------|");
            System.out.println("|  1: Create Event             |");
            System.out.println("|  2: Remove Event             |");
            System.out.println("|  3: Update Event             |");
            System.out.println("|  4: Manage Sessions          |");
            System.out.println("|  5: Manage Speakers          |");
            System.out.println("|  6: View All Events          |");
            System.out.println("|  7: View All Ticket Type     |");
            System.out.println("|  8: Update Ticket Type       |");
            System.out.println("|  9: View My Profile          |");
            System.out.println("|  0: Back to Main Menu        |");
            System.out.println("--------------------------------");
            System.out.print("Enter option: ");
            int choice = readInt();

            switch (choice) {
                case 1:
                    createEvent();
                    break;
                case 2:
                    removeEvent();
                    break;
                case 3:
                    updateEvent();
                    break;
                case 4:
                    manageSessionsMenu();
                    break;
                case 5:
                    manageSpeakersMenu();
                    break;
                case 6:
                    viewAllEvents();
                    break;
                case 7:
                    viewAllTicketType();
                    break;
                case 8:
                    UpdateTicketType();
                    break;
                case 9:
                    Organizer currentOrganizer = (Organizer) ems.getCuurent_User();
                    currentOrganizer.displayProfile();
                    waitForEnter();
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // CREATE EVENT
    // -------------------------------------------------------------------------
    static void createEvent() {
        System.out.println("\n--- Create Event ---");
        System.out.println("1: Concert");
        System.out.println("2: Workshop");
        System.out.println("3: Conference");
        System.out.println("0: Cancel");
        System.out.print("Enter option: ");
        int type = readInt();

        if (type == 0) {
            System.out.println("Cancelled.");
            return;
        }
        if (type < 1 || type > 3) {
            System.out.println("Invalid event type.");
            return;
        }

        // common fields
        String title;
        do {
            System.out.print("Title            : ");
            title = scan.nextLine();
        } while (!validationTitle(title));

        String date;
        LocalDate parsedDate;
        do {
            System.out.print("Date (YYYY-MM-DD) : ");
            date = scan.nextLine();
            parsedDate = ems.validationDate(date);
        } while (parsedDate == null);

        String venue;
        do {
            System.out.print("Venue            : ");
            venue = scan.nextLine();
        } while (!validationVenue(venue));

        int qeb = 0;
        int qsd = 0;
        int qvip = 0;
        int maxTix = 0;
        do {
            try {
                System.out.print("\nCreating ticket type......");
                scan.nextLine();
                System.out.print("\nMax Ticket (Recommend 150):");
                maxTix = readInt();
                System.out.print("Quantity Early Bird (Recommend 20% of total ticket):");
                qeb = readInt();
                System.out.print("Quantity Standard (Recommend 60% of total ticket):");
                qsd = readInt();
                System.out.print("Quantity Vip (Recommend 20% of total ticket):");
                qvip = readInt();
            } catch (Exception e) {
                System.out.println("Invalid input. Please retry.");
            }
        } while (!validationQuantityTicket(maxTix, qeb, qsd, qvip));

        double peb = 0.0;
        double psd = 0.0;
        double pvip = 0.0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("\nPrice Early Bird (RM):");
                peb = scan.nextDouble();
                scan.nextLine();
                System.out.print("Price Standard (RM):");
                psd = scan.nextDouble();
                scan.nextLine();
                System.out.print("Price Vip (RM):");
                pvip = scan.nextDouble();
                scan.nextLine();

                if (validationPrice(peb, psd, pvip)) {
                    validInput = true;
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter numbers only.");
                scan.nextLine(); // Clear buffer
            }
        }

        String perks;
        do {
            System.out.print("\nPerks Provided: (if no just enter -) ");
            perks = scan.nextLine();
        } while (!validationPerks(perks));

        String ssdate;
        LocalDate salesStartDate;
        do {
            System.out.print("Sales Start Date (YYYY-MM-DD) : ");
            ssdate = scan.nextLine();
            salesStartDate = validationSalesStartDate(ssdate, parsedDate);
        } while (salesStartDate == null);

        String sedate;
        LocalDate salesEndDate;
        do {
            System.out.print("Sales End Date (YYYY-MM-DD) : ");
            sedate = scan.nextLine();
            salesEndDate = validationSalesEndDate(sedate, salesStartDate, parsedDate);
        } while (salesEndDate == null);

        System.out.println("Ticket type created.");

        if (type == 1) {
            // Concert
            Concert c = new Concert(title, parsedDate, venue, maxTix);
            TicketType tt = new TicketType(c.getEventID(), maxTix, qeb, qsd, qvip, maxTix, qeb, qsd, qvip, peb, psd,
                    pvip, perks, salesStartDate, salesEndDate);
            ticketTypes.add(tt);
            storeTicketTypeData(ticketTypes);
            concerts.add(c);
            appendToConcertFile(c);
            events[eventCount++] = c;
            System.out.println("Concert created successfully : " + c.getEventID());

            // Ask if organizer wants to assign speakers now
            System.out.print("\nDo you want to assign speakers to this concert now? (1=Yes / 0=No): ");
            int assignnow = readInt();
            if (assignnow == 1) {
                assignSpeakerToConcertOrWorkshop(c, null);
                storeConcertData(concerts);
            }

        } else if (type == 2) {
            // Workshop
            Workshop w = new Workshop(title, parsedDate, venue, maxTix);
            TicketType tt = new TicketType(w.getEventID(), maxTix, qeb, qsd, qvip, maxTix, qeb, qsd, qvip, peb, psd,
                    pvip, perks, salesStartDate, salesEndDate);
            ticketTypes.add(tt);
            storeTicketTypeData(ticketTypes);
            workshops.add(w);
            appendToWorkshopFile(w);

            events[eventCount++] = w;
            System.out.println("Workshop created successfully : " + w.getEventID());

            // Ask if organizer wants to assign speakers now
            System.out.print("\nDo you want to assign speakers to this workshop now? (1=Yes / 0=No): ");
            int assignnow = readInt();
            if (assignnow == 1) {
                assignSpeakerToConcertOrWorkshop(null, w);
                storeWorkshopData(workshops);
            }

        } else {
            // Conference ask how many sessions to create right away
            System.out.print("How many sessions to create now? (0 to skip): ");
            int numSessions = readInt();

            String[] topics = new String[numSessions];
            String[] times = new String[numSessions];

            for (int i = 0; i < numSessions; i++) {
                System.out.print("  Session " + (i + 1) + " Topic            : ");
                topics[i] = scan.nextLine();

                // validate time in HHMM format
                do {
                    System.out.print("  Session " + (i + 1) + " Time (HHMM 0000-2359): ");
                    times[i] = scan.nextLine();
                } while (!validationSessionTime(times[i]));
            }

            Conference conf = new Conference(title, parsedDate, venue, maxTix);
            TicketType tt = new TicketType(conf.getEventID(), maxTix, qeb, qsd, qvip, maxTix, qeb, qsd, qvip, peb, psd,
                    pvip, perks, salesStartDate, salesEndDate);
            ticketTypes.add(tt);
            storeTicketTypeData(ticketTypes);
            if (numSessions > 0) {
                conf.autoCreateSessions(topics, times);
            }
            conferences.add(conf);

            events[eventCount++] = conf;
            int eventIdx = 0;
            if (eventCount != 0) {
                eventIdx = eventCount - 1;
            }
            saveEvent(events[eventIdx]);
            System.out.println("Conference created successfully : " + conf.getEventID());
        }
    }

    // -------------------------------------------------------------------------
    // REMOVE EVENT
    // -------------------------------------------------------------------------
    static void removeEvent() {
        if (eventCount == 0) {
            System.out.println("No events available.");
            return;
        }
        System.out.println("\n--- Remove Event ---");
        listEvents();
        System.out.print("Enter Event ID to remove: ");
        String eventID = scan.nextLine();

        boolean found = false;

        for (int i = 0; i < eventCount; i++) {
            if (events[i].hasEvent(eventID)) {
                if (events[i].isConcert()) {
                    Concert.removeConcert(concerts, eventID);
                    storeConcertData(concerts);
                } else if (events[i].isWorkshop()) {
                    Workshop.removeWorkshop(workshops, eventID);
                    storeWorkshopData(workshops);
                } else if (events[i].isConference()) {
                    Conference.removeConference(conferences, eventID);
                    storeConferenceData();
                    // remove from flat events array
                    for (int z = i; z < eventCount - 1; z++) {
                        events[z] = events[z + 1];
                    }
                    events[eventCount - 1] = null;
                    eventCount--;
                    found = true;
                    break;
                }
            }

        }
        if (!found) {
            System.out.println("Error: Event [" + eventID + "] not found !");
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE EVENT
    // -------------------------------------------------------------------------
    static void updateEvent() {
        if (eventCount == 0) {
            System.out.println("No events available to update.");
            return;
        }
        System.out.println("\n--- Update Event ---");
        listEvents();
        System.out.print("Select event number (0 to cancel): ");
        int idx = readInt() - 1;

        if (idx == -1) {
            System.out.println("Cancelled.");
            return;
        }
        if (idx < 0 || idx >= eventCount) {
            System.out.println("Invalid selection.");
            return;
        }

        Event e = events[idx];

        // Show current event details before asking what to change
        System.out.println();
        System.out.println("  --------------------------------------------");
        System.out.printf("  |  Current Event Details                   |%n");
        System.out.println("  |------------------------------------------|");
        System.out.printf("  |  ID       : %-28s |%n", e.getEventID());
        System.out.printf("  |  Type     : %-28s |%n", e.getClass().getSimpleName());
        System.out.printf("  |  Title    : %-28s |%n",
                e.getTitle().length() > 28 ? e.getTitle().substring(0, 25) + "..." : e.getTitle());
        System.out.printf("  |  Date     : %-28s |%n", e.getDate());
        System.out.printf("  |  Venue    : %-28s |%n",
                e.getVenue().length() > 28 ? e.getVenue().substring(0, 25) + "..." : e.getVenue());
        System.out.printf("  |  MaxTix   : %-28d |%n", e.getMaxTickets());
        System.out.println("  --------------------------------------------");

        System.out.println("\n  What to update?");
        System.out.println("  1: Title");
        System.out.println("  2: Date");
        System.out.println("  3: Venue");
        System.out.println("  4: Max Tickets");
        System.out.println("  0: Cancel");
        System.out.print("Enter option: ");
        int field = readInt();

        if (field == 0) {
            System.out.println("Cancelled.");
            return;
        }

        switch (field) {
            case 1:
                String newTitle;
                do {
                    System.out.print("New Title       : ");
                    newTitle = scan.nextLine();
                } while (!validationTitle(newTitle));
                e.setTitle(newTitle);
                saveEvent(e);
                System.out.println("Title updated.");
                break;
            case 2:
                LocalDate newDate = null;
                do {
                    System.out.print("New Date (YYYY-MM-DD): ");
                    String newDateStr = scan.nextLine();
                    newDate = ems.validationDate(newDateStr);
                } while (newDate == null);
                e.setDate(newDate);
                saveEvent(e);
                System.out.println("Date updated.");
                break;
            case 3:
                String newVenue;
                do {
                    System.out.print("New Venue       : ");
                    newVenue = scan.nextLine();
                } while (!validationVenue(newVenue));
                e.setVenue(newVenue);
                saveEvent(e);
                System.out.println("Venue updated.");
                break;
            case 4:
                int newMax;
                do {
                    System.out.print("New Max Tickets : ");
                    newMax = readInt();
                } while (!validationMaxTickets(newMax));
                e.setMaxTickets(newMax);
                saveEvent(e);
                System.out.println("Max tickets updated.");
                break;
            default:
                System.out.println("Invalid option.");
        }

    }

    // -------------------------------------------------------------------------
    // MANAGE SESSIONS MENU
    // -------------------------------------------------------------------------
    static void manageSessionsMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--------------------------------");
            System.out.println("|      MANAGE SESSIONS         |");
            System.out.println("|------------------------------|");
            System.out.println("|  1: Add Session              |");
            System.out.println("|  2: Remove Session           |");
            System.out.println("|  3: View Sessions            |");
            System.out.println("|  0: Back                     |");
            System.out.println("--------------------------------");
            System.out.print("Enter option: ");
            int choice = readInt();

            switch (choice) {
                case 1:
                    addSession();
                    break;
                case 2:
                    removeSession();
                    break;
                case 3:
                    viewSessions();
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // add a session to a conference
    static void addSession() {
        Conference conf = selectConference();
        if (conf == null) {
            return;
        }
        System.out.print("Session Topic            : ");
        String topic = scan.nextLine();

        String time;
        do {
            System.out.print("Session Time (HHMM 0000-2359): ");
            time = scan.nextLine();
        } while (!validationSessionTime(time));

        Session s = conf.createSession(topic, time);
        updateInFile(conf);
        if (s != null) {
            System.out.println("Session [" + s.getSessionID() + "] added to conference [" + conf.getEventID() + "].");
        }
    }

    // remove a session from a conference
    static void removeSession() {
        Conference conf = selectConference();
        if (conf == null) {
            return;
        }
        if (conf.getSessionCount() == 0) {
            System.out.println("No sessions in this conference.");
            return;
        }
        conf.displaySessions();
        System.out.print("Select session number to remove (0 to cancel): ");
        int idx = readInt() - 1;
        if (idx == -1) {
            System.out.println("Cancelled.");
            return;
        }
        if (idx < 0 || idx >= conf.getSessionCount()) {
            System.out.println("Invalid selection.");
            return;
        }
        String sessionID = conf.getSessions()[idx].getSessionID();
        conf.removeSession(sessionID);
        saveEvent(conf); // auto-save after session removed
    }

    // view sessions of a conference
    static void viewSessions() {
        Conference conf = selectConference();
        if (conf == null) {
            return;
        }
        conf.displaySessions();
    }

    // helper: pick a conference from the list
    static Conference selectConference() {
        int count = 0;
        Conference[] confList = new Conference[eventCount];
        for (int i = 0; i < eventCount; i++) {
            if (events[i].isConference()) {
                confList[count++] = (Conference) events[i];
            }
        }
        if (count == 0) {
            System.out.println("No conferences found. Create a Conference event first.");
            return null;
        }
        System.out.println("\n--- Select Conference ---");
        for (int i = 0; i < count; i++) {
            System.out.println("  " + (i + 1) + ": [" + confList[i].getEventID() + "] " + confList[i].getTitle());
        }
        System.out.println("  0: Cancel");
        System.out.print("Select conference number: ");
        int idx = readInt() - 1;
        if (idx == -1) {
            System.out.println("Cancelled.");
            return null;
        }
        if (idx < 0 || idx >= count) {
            System.out.println("Invalid selection.");
            return null;
        }
        return confList[idx];
    }

    // validate title (must not be empty and at least 3 characters)
    public static boolean validationTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Error: Title cannot be empty !");
            return false;
        } else if (title.trim().length() < 3) {
            System.out.println("Error: Title must be at least 3 characters !");
            return false;
        } else {
            return true;
        }
    }

    // validate date string (must be YYYY-MM-DD format and a future date)
    // returns the parsed LocalDate if valid, or null if invalid
    public static LocalDate validationDate(String date) {
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

    // validate venue (must not be empty)
    public static boolean validationVenue(String venue) {
        if (venue == null || venue.trim().isEmpty()) {
            System.out.println("Error: Venue cannot be empty !");
            return false;
        } else {
            return true;
        }
    }

    // validate max tickets (must be greater than 0)
    public static boolean validationMaxTickets(int maxTickets) {
        if (maxTickets <= 0) {
            System.out.println("Error: Max tickets must be greater than 0 !");
            return false;
        } else {
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // MANAGE SPEAKERS MENU
    // -------------------------------------------------------------------------
    static void manageSpeakersMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n------------------------------------");
            System.out.println("|        MANAGE SPEAKERS           |");
            System.out.println("-----------------------------------|");
            System.out.println("|  1: Assign Speaker to Conference |");
            System.out.println("|     Session                      |");
            System.out.println("|  2: Remove Speaker from          |");
            System.out.println("|     Conference Session           |");
            System.out.println("|  3: Update/Remove Speaker in     |");
            System.out.println("|     Concert                      |");
            System.out.println("|  4: Update/Remove Speaker in     |");
            System.out.println("|     Workshop                     |");
            System.out.println("|  5: View Speakers                |");
            System.out.println("|  0: Back                         |");
            System.out.println("------------------------------------");
            System.out.print("Enter option: ");
            int choice = readInt();

            switch (choice) {
                case 1:
                    assignSpeakerToConferenceSession();
                    break;
                case 2:
                    removeSpeakerFromConferenceSession();
                    break;
                case 3:
                    manageConcertSpeakers();
                    break;
                case 4:
                    manageWorkshopSpeakers();
                    break;
                case 5:
                    viewSpeakers();
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // Ensure speaker file exists (create if not)
    public static void ensureSpeakerFileExists() {
        File speakerFile = new File("speaker.json");
        if (!speakerFile.exists()) {
            try {
                if (speakerFile.createNewFile()) {
                    System.out.println("Speaker file created: " + speakerFile.getName());
                }
            } catch (IOException e) {
                System.out.println("Error creating speaker file: " + e.getMessage());
            }
        }
    }

    // validate session time (must be HHMM format, 0000 - 2359)
    public static boolean validationSessionTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            System.out.println("Error: Session time cannot be empty !");
            return false;
        }
        if (time.trim().length() != 4) {
            System.out.println("Error: Session time must be 4 digits in HHMM format (e.g. 0900, 1430) !");
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (!Character.isDigit(time.trim().charAt(i))) {
                System.out.println("Error: Session time must contain digits only (e.g. 0900, 1430) !");
                return false;
            }
        }
        int hh = Integer.parseInt(time.trim().substring(0, 2));
        int mm = Integer.parseInt(time.trim().substring(2, 4));
        if (hh < 0 || hh > 23) {
            System.out.println("Error: Hour must be between 00 and 23 !");
            return false;
        }
        if (mm < 0 || mm > 59) {
            System.out.println("Error: Minute must be between 00 and 59 !");
            return false;
        }
        return true;
    }

    static void loadSpeakersFromUsers(User[] alluser, int totalUsers) {
        speakerCount = 0;
        Speaker usertemp = new Speaker();
        for (int i = 0; i < totalUsers; i++) {
            if (usertemp.checkClass(alluser[i])) {
                speakerPool[speakerCount++] = (Speaker) alluser[i];
            }
        }

        // Update the static 'no' variable in Speaker class
        Speaker.setTotalSpeakers(speakerCount);

        // Load bios from speaker.json
        loadAllSpeakerBios();

        if (speakerCount > 0) {
            System.out.println(speakerCount + " speaker(s) loaded from user accounts.");
        }
    }

    // Helper class to store speaker data
    static class SpeakerData {
        String bio = "No bio available";
        Map<String, String> sessionTopics = new HashMap<>();
    }

    // Save speaker bio and session topics to speaker.json
    public static void saveSpeakerData(String username, String bio, Map<String, String> sessionTopics) {
        try {
            // Load existing data
            Map<String, SpeakerData> allSpeakerData = loadAllSpeakerData();

            // Update or create speaker data
            SpeakerData data = allSpeakerData.getOrDefault(username, new SpeakerData());
            data.bio = (bio != null && !bio.isEmpty()) ? bio : "No bio available";
            data.sessionTopics = sessionTopics;
            allSpeakerData.put(username, data);

            // Write all data back to file
            try (Writer writer = new FileWriter("speaker.json")) {
                for (Map.Entry<String, SpeakerData> entry : allSpeakerData.entrySet()) {
                    writer.write(entry.getKey() + "\n");
                    writer.write(entry.getValue().bio + "\n");
                    // Save session topics
                    writer.write(entry.getValue().sessionTopics.size() + "\n");
                    for (Map.Entry<String, String> topic : entry.getValue().sessionTopics.entrySet()) {
                        writer.write(topic.getKey() + "\n");
                        writer.write(topic.getValue() + "\n");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving speaker data: " + e.getMessage());
        }
    }

    // Load all speaker data from speaker.json
    public static Map<String, SpeakerData> loadAllSpeakerData() {
        Map<String, SpeakerData> allSpeakerData = new HashMap<>();
        try {
            File dataFile = new File("speaker.json");
            if (!dataFile.exists()) {
                return allSpeakerData;
            }

            List<String> lines = Files.readAllLines(Paths.get("speaker.json"));
            int i = 0;
            while (i < lines.size()) {
                if (i + 1 >= lines.size())
                    break;

                String username = lines.get(i++);
                String bio = lines.get(i++);

                SpeakerData data = new SpeakerData();
                data.bio = bio;

                // Check if there's a topic count (new format)
                if (i < lines.size()) {
                    try {
                        int topicCount = Integer.parseInt(lines.get(i));
                        i++; // move past topicCount

                        for (int j = 0; j < topicCount && i + 1 < lines.size(); j++) {
                            String sessionId = lines.get(i++);
                            String topic = lines.get(i++);
                            data.sessionTopics.put(sessionId, topic);
                        }
                    } catch (NumberFormatException e) {

                    }
                }

                allSpeakerData.put(username, data);
            }
        } catch (IOException e) {
            System.out.println("Error loading speaker data: " + e.getMessage());
        }
        return allSpeakerData;
    }

    // Save speaker bio only (for backward compatibility)
    public static void saveSpeakerBio(String username, String bio) {
        Map<String, SpeakerData> allData = loadAllSpeakerData();
        SpeakerData data = allData.getOrDefault(username, new SpeakerData());
        data.bio = (bio != null && !bio.isEmpty()) ? bio : "No bio available";
        allData.put(username, data);
        saveAllSpeakerData(allData);
    }

    // Save all speaker data to file
    public static void saveAllSpeakerData(Map<String, SpeakerData> allData) {
        try (Writer writer = new FileWriter("speaker.json")) {
            for (Map.Entry<String, SpeakerData> entry : allData.entrySet()) {
                writer.write(entry.getKey() + "\n");
                writer.write(entry.getValue().bio + "\n");
                writer.write(entry.getValue().sessionTopics.size() + "\n");
                for (Map.Entry<String, String> topic : entry.getValue().sessionTopics.entrySet()) {
                    writer.write(topic.getKey() + "\n");
                    writer.write(topic.getValue() + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving speaker data: " + e.getMessage());
        }
    }

    // Save updated session topic for a speaker
    public static void saveSpeakerSessionTopic(String username, String sessionId, String newTopic) {
        Map<String, SpeakerData> allData = loadAllSpeakerData();
        SpeakerData data = allData.getOrDefault(username, new SpeakerData());
        data.sessionTopics.put(sessionId, newTopic);
        allData.put(username, data);
        saveAllSpeakerData(allData);
    }

    // Load session topic for a speaker
    public static String loadSpeakerSessionTopic(String username, String sessionId) {
        Map<String, SpeakerData> allData = loadAllSpeakerData();
        SpeakerData data = allData.get(username);
        if (data != null && data.sessionTopics.containsKey(sessionId)) {
            return data.sessionTopics.get(sessionId);
        }
        return null;
    }

    // Update loadAllSpeakerBios to also load session topics
    public static void loadAllSpeakerBios() {
        Map<String, SpeakerData> allData = loadAllSpeakerData();
        for (int j = 0; j < speakerCount; j++) {
            String username = speakerPool[j].getUsername();
            SpeakerData data = allData.get(username);
            if (data != null) {
                speakerPool[j].setBio(data.bio);
                // Store session topics in a map within the speaker object
                speakerPool[j].setSessionTopics(data.sessionTopics);
            }
        }
    }
    // -- Conference session speaker management (original, renamed) -------------

    // assign a speaker from the pool to a conference session
    static void assignSpeakerToConferenceSession() {
        // Refresh pool so speakers added this session are visible
        loadSpeakersFromUsers(ems.getUsers(), countUsers(ems.getUsers()));
        if (speakerCount == 0) {
            System.out.println("No speakers available. Create a Speaker account first (sign up with password 54321).");
            return;
        }
        Conference conf = selectConference();
        if (conf == null)
            return;
        if (conf.getSessionCount() == 0) {
            System.out.println("No sessions in this conference. Add a session first.");
            return;
        }
        conf.displaySessions();
        System.out.print("Select session number to assign speaker to (0 to cancel): ");
        int sIdx = readInt() - 1;
        if (sIdx == -1) {
            System.out.println("Cancelled.");
            return;
        }
        if (sIdx < 0 || sIdx >= conf.getSessionCount()) {
            System.out.println("Invalid selection.");
            return;
        }
        Session targetSession = conf.getSessions()[sIdx];
        viewSpeakers();
        System.out.print("Select speaker number (0 to cancel): ");
        int spIdx = readInt() - 1;
        if (spIdx == -1) {
            System.out.println("Cancelled.");
            return;
        }
        if (spIdx < 0 || spIdx >= speakerCount) {
            System.out.println("Invalid selection.");
            return;
        }
        ems.assignSpeaker(targetSession, speakerPool[spIdx], conf);

        storeConferenceData();

    }

    // remove a speaker from a conference session
    static void removeSpeakerFromConferenceSession() {
        Conference conf = selectConference();
        if (conf == null)
            return;
        if (conf.getSessionCount() == 0) {
            System.out.println("No sessions in this conference.");
            return;
        }
        conf.displaySessions();
        System.out.print("Select session number (0 to cancel): ");
        int sIdx = readInt() - 1;
        if (sIdx == -1) {
            System.out.println("Cancelled.");
            return;
        }
        if (sIdx < 0 || sIdx >= conf.getSessionCount()) {
            System.out.println("Invalid selection.");
            return;
        }
        Session targetSession = conf.getSessions()[sIdx];
        System.out.print("Enter Speaker Username to remove: ");
        String speakerUsername = scan.nextLine();
        ems.removeSpeaker(targetSession, speakerUsername);
        storeConferenceData();
    }

    // -- Concert speaker management --------------------------------------------

    /**
     * Unified: assign, remove, or change speakers for a Concert.
     * Also called from post-creation prompt (passing concert directly).
     */
    static void manageConcertSpeakers() {
        Concert concert = selectConcert();
        if (concert == null)
            return;
        manageConcertSpeakers(concert);
    }

    static void manageConcertSpeakers(Concert concert) {
        System.out.println("\n--------------------------------");
        System.out.println("|   CONCERT SPEAKERS MENU      |");
        System.out.println("|------------------------------|");
        System.out.println("|  1: Assign Speaker           |");
        System.out.println("|  2: Update Speaker           |");
        System.out.println("|  3: Remove Speaker           |");
        System.out.println("|  0: Back                     |");
        System.out.println("--------------------------------");
        System.out.print("Enter option: ");
        int opt = readInt();

        if (opt == 1) {
            assignSpeakerToConcertOrWorkshop(concert, null);
            storeConcertData(concerts);
        } else if (opt == 2) {
            if (concert.getSpeakerCount() == 0) {
                System.out.println("No speakers assigned to this concert yet.");
                return;
            }
            concert.displaySpeakers();
            System.out.print("Enter Speaker Username to replace: ");
            String oldUname = scan.nextLine();
            viewSpeakers();
            System.out.print("Select new speaker number (0 to cancel): ");
            int spIdx = readInt() - 1;
            if (spIdx == -1) {
                System.out.println("Cancelled.");
                return;
            }
            if (spIdx < 0 || spIdx >= speakerCount) {
                System.out.println("Invalid selection.");
                return;
            }
            concert.changeSpeaker(oldUname, speakerPool[spIdx].getUsername());
            storeConcertData(concerts);
        } else if (opt == 3) {
            if (concert.getSpeakerCount() == 0) {
                System.out.println("No speakers assigned to this concert yet.");
                return;
            }
            concert.displaySpeakers();
            System.out.print("Enter Speaker Username to remove: ");
            String uname = scan.nextLine();
            concert.removeSpeaker(uname);
            storeConcertData(concerts);
        } else if (opt != 0) {
            System.out.println("Invalid option.");
        }
    }

    // -- Workshop speaker management -------------------------------------------

    /**
     * Unified: assign, remove, or change speakers for a Workshop.
     * Also called from post-creation prompt (passing workshop directly).
     */
    static void manageWorkshopSpeakers() {
        Workshop workshop = selectWorkshop();
        if (workshop == null)
            return;
        manageWorkshopSpeakers(workshop);
    }

    static void manageWorkshopSpeakers(Workshop workshop) {
        System.out.println("\n--------------------------------");
        System.out.println("|   WORKSHOP SPEAKERS MENU     |");
        System.out.println("|------------------------------|");
        System.out.println("|  1: Assign Speaker           |");
        System.out.println("|  2: Update Speaker           |");
        System.out.println("|  3: Remove Speaker           |");
        System.out.println("|  0: Back                     |");
        System.out.println("--------------------------------");
        System.out.print("Enter option: ");
        int opt = readInt();

        if (opt == 1) {
            assignSpeakerToConcertOrWorkshop(null, workshop);
            storeWorkshopData(workshops);
        } else if (opt == 2) {
            if (workshop.getSpeakerCount() == 0) {
                System.out.println("No speakers assigned to this workshop yet.");
                return;
            }
            workshop.displaySpeakers();
            System.out.print("Enter Speaker Username to replace: ");
            String oldUname = scan.nextLine();
            viewSpeakers();
            System.out.print("Select new speaker number (0 to cancel): ");
            int spIdx = readInt() - 1;
            if (spIdx == -1) {
                System.out.println("Cancelled.");
                return;
            }
            if (spIdx < 0 || spIdx >= speakerCount) {
                System.out.println("Invalid selection.");
                return;
            }
            workshop.changeSpeaker(oldUname, speakerPool[spIdx].getUsername());
            storeWorkshopData(workshops);
        } else if (opt == 3) {
            if (workshop.getSpeakerCount() == 0) {
                System.out.println("No speakers assigned to this workshop yet.");
                return;
            }
            workshop.displaySpeakers();
            System.out.print("Enter Speaker Username to remove: ");
            String uname = scan.nextLine();
            workshop.removeSpeaker(uname);
            storeWorkshopData(workshops);
        } else if (opt != 0) {
            System.out.println("Invalid option.");
        }
    }

    // -- Shared helper: assign speakers loop for Concert or Workshop -----------

    /**
     * Prompts the user to assign speakers one by one.
     * Pass either a Concert or Workshop (the other must be null).
     */
    static void assignSpeakerToConcertOrWorkshop(Concert concert, Workshop workshop) {
        // Refresh pool so speakers added this session are visible
        loadSpeakersFromUsers(ems.getUsers(), countUsers(ems.getUsers()));
        if (speakerCount == 0) {
            System.out.println("No speakers available. Create a Speaker account first (sign up with password 54321).");
            return;
        }
        String eventLabel = (concert != null) ? "concert" : "workshop";
        boolean keepAssigning = true;
        while (keepAssigning) {
            viewSpeakers();
            System.out.print("Select speaker number to assign (0 to stop): ");
            int spIdx = readInt() - 1;
            if (spIdx == -1) {
                keepAssigning = false;
            } else if (spIdx < 0 || spIdx >= speakerCount) {
                System.out.println("Invalid selection.");
            } else {
                Speaker sp = speakerPool[spIdx];
                if (concert != null) {
                    concert.assignSpeaker(sp.getUsername());
                    storeConcertData(concerts);
                } else {
                    workshop.assignSpeaker(sp.getUsername());
                }
                System.out.print("Assign another speaker to this " + eventLabel + "? (1=Yes / 0=No): ");
                int cont = readInt();
                if (cont != 1)
                    keepAssigning = false;
            }
        }
    }

    // -- Select helpers --------------------------------------------------------

    /** Let the user pick a Concert from the loaded list. */
    static Concert selectConcert() {
        int count = 0;
        Concert[] concertList = new Concert[eventCount];
        for (int i = 0; i < eventCount; i++) {
            if (events[i].isConcert()) {
                concertList[count++] = (Concert) events[i];
            }
        }
        if (count == 0) {
            System.out.println("No concerts found. Create a Concert event first.");
            return null;
        }
        System.out.println("\n--- Select Concert ---");
        for (int i = 0; i < count; i++) {
            System.out.println("  " + (i + 1) + ": [" + concertList[i].getEventID() + "] "
                    + concertList[i].getTitle());
        }
        System.out.println("  0: Cancel");
        System.out.print("Select concert number: ");
        int idx = readInt() - 1;
        if (idx == -1) {
            System.out.println("Cancelled.");
            return null;
        }
        if (idx < 0 || idx >= count) {
            System.out.println("Invalid selection.");
            return null;
        }
        return concertList[idx];
    }

    /** Let the user pick a Workshop from the loaded list. */
    static Workshop selectWorkshop() {
        int count = 0;
        Workshop[] workshopList = new Workshop[eventCount];
        for (int i = 0; i < eventCount; i++) {
            if (events[i].isWorkshop()) {
                workshopList[count++] = (Workshop) events[i];
            }
        }
        if (count == 0) {
            System.out.println("No workshops found. Create a Workshop event first.");
            return null;
        }
        System.out.println("\n--- Select Workshop ---");
        for (int i = 0; i < count; i++) {
            System.out.println("  " + (i + 1) + ": [" + workshopList[i].getEventID() + "] "
                    + workshopList[i].getTitle());
        }
        System.out.println("  0: Cancel");
        System.out.print("Select workshop number: ");
        int idx = readInt() - 1;
        if (idx == -1) {
            System.out.println("Cancelled.");
            return null;
        }
        if (idx < 0 || idx >= count) {
            System.out.println("Invalid selection.");
            return null;
        }
        return workshopList[idx];
    }

    // view all registered speakers
    static void viewSpeakers() {
        // Refresh speakerPool so newly created speaker accounts are always included
        loadSpeakersFromUsers(ems.getUsers(), countUsers(ems.getUsers()));
        if (speakerCount == 0) {
            System.out.println("No speakers available. Create a Speaker account first (sign up with password 54321).");
            return;
        }
        System.out.println("\n--- Available Speakers ---");
        System.out.printf("%-5s %-15s %-25s%n", "No.", "Username", "Email");
        System.out.println("--------------------------------------------");
        for (int i = 0; i < speakerCount; i++) {
            System.out.printf("%-5d %-15s %-25s%n", (i + 1),
                    speakerPool[i].getUsername(),
                    speakerPool[i].getEmail());
        }
    }

    // -------------------------------------------------------------------------
    // VIEW ALL EVENTS
    // -------------------------------------------------------------------------
    static void viewAllEvents() {
        if (eventCount == 0) {
            System.out.println("\n  No events created yet.");
            return;
        }

        // Speaker column width = 12; total table width = 101 chars (fits ~102-col
        // terminal)
        String divider = "  |----|------|------------|-----------------|------------|-----------------|--------------|--------|";
        String top = "  ---------------------------------------------------------------------------------------------------";
        String mid = "  |----|------|------------|-----------------|------------|-----------------|--------------|--------|";
        String bot = "  ---------------------------------------------------------------------------------------------------";

        System.out.println(
                "\n  ---------------------------------------------------------------------------------------------------");
        System.out.println(
                "  |                                           ALL EVENTS                                            |");
        System.out.println(
                "  ---------------------------------------------------------------------------------------------------");
        System.out.println(top);
        System.out.printf("  | %-2s | %-4s | %-10s | %-15s | %-10s | %-15s | %-12s | %-6s |%n",
                "No", "ID", "Type", "Title", "Date", "Venue", "Speaker", "MaxTix");
        System.out.println(divider);

        for (int i = 0; i < eventCount; i++) {
            Event e = events[i];
            String type = e.getClass().getSimpleName();

            // truncate long fields so columns stay fixed-width
            String title = e.getTitle().length() > 15 ? e.getTitle().substring(0, 12) + "..." : e.getTitle();
            String venue = e.getVenue().length() > 15 ? e.getVenue().substring(0, 12) + "..." : e.getVenue();

            // build speaker string for the dedicated column
            String speakerCol = "";
            if (e.isConcert()) {
                Concert c = (Concert) e;
                if (c.getSpeakerCount() > 0) {
                    speakerCol = c.getSpeakers()[0];
                }
            } else if (e.isWorkshop()) {
                Workshop w = (Workshop) e;
                if (w.getSpeakerCount() > 0) {
                    speakerCol = w.getSpeakers()[0];
                }
            }
            // Conference: speakers belong to individual sessions leave column empty
            // Truncate name if too long to avoid breaking column alignment
            if (speakerCol.length() > 12) {
                speakerCol = speakerCol.substring(0, 9) + "...";
            }

            System.out.printf("  | %-2d | %-4s | %-10s | %-15s | %-10s | %-15s | %-12s | %6d |%n",
                    (i + 1), e.getEventID(), type, title, e.getDate(), venue, speakerCol, e.getMaxTickets());

            // Conference: show sessions as sub-rows below the event row
            // Sub-row spans Title+Date+Venue (45 chars) then leaves Speaker column blank
            if (e.isConference()) {
                Conference conf = (Conference) e;
                if (conf.getSessionCount() == 0) {
                    System.out.printf("  |    |      |            |  %-45s |              |        |%n",
                            "(no sessions)");
                } else {
                    for (int s = 0; s < conf.getSessionCount(); s++) {
                        Session sess = conf.getSessions()[s];
                        String detail = String.format("  [%s] %s @ %s",
                                sess.getSessionID(), sess.getTopic(), sess.getTime());
                        if (detail.length() > 45)
                            detail = detail.substring(0, 42) + "...";
                        System.out.printf("  |    |      |            |  %-45s |              |        |%n",
                                detail);
                    }
                }
            }

            if (i < eventCount - 1)
                System.out.println(mid);
        }
        System.out.println(bot);
        System.out.println("  the total event = " + eventCount);
    }

    // count non-null users in the array
    static int countUsers(User[] users) {
        int count = 0;
        for (User u : users) {
            if (u != null)
                count++;
        }
        return count;
    }

    // helper: print numbered event list (used by other methods)
    static void listEvents() {
        System.out.println(
                "  ------------------------------------------------------------------------------------------------");
        System.out.printf("  | %-2s | %-4s | %-12s | %-20s | %-10s | %-20s | %-6s |%n",
                "No", "ID", "Type", "Title", "Date", "Venue", "MaxTix");
        System.out.println(
                "  |----|------|--------------|----------------------|------------|----------------------|--------|");
        for (int i = 0; i < eventCount; i++) {
            Event e = events[i];
            String type = e.getClass().getSimpleName();
            String title = e.getTitle().length() > 20 ? e.getTitle().substring(0, 17) + "..." : e.getTitle();
            String venue = e.getVenue().length() > 20 ? e.getVenue().substring(0, 17) + "..." : e.getVenue();
            System.out.printf("  | %-2d | %-4s | %-12s | %-20s | %-10s | %-20s | %6d |%n",
                    (i + 1), e.getEventID(), type, title, e.getDate(), venue, e.getMaxTickets());
        }
        System.out.println(
                "  ------------------------------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // SAVE / LOAD ALL EVENTS
    // -------------------------------------------------------------------------
    // Auto-save a single event's list to the correct JSON file based on its type
    static void saveEvent(Event e) {
        if (e.isConcert()) {
            storeConcertData(concerts);
        } else if (e.isWorkshop()) {
            storeWorkshopData(workshops);
        } else if (e.isConference()) {
            storeConferenceData();
        }
    }

    static void saveAllEvents() {
        storeConcertData(concerts);
        storeWorkshopData(workshops);
        storeConferenceData();
        storeTicketTypeData(ticketTypes);
        System.out.println("All events saved successfully.");
    }

    static void loadAllEvents() {
        concerts.clear();
        workshops.clear();
        conferences.clear();
        ticketTypes.clear();
        eventCount = 0;

        concerts = readConcertData();
        workshops = readWorkshopData();
        conferences = readConferenceData();
        ticketTypes = readTicektTypeData(); // load ticket types so purchase works
        for (Concert c : concerts) {
            events[eventCount++] = c;
        }
        for (Workshop w : workshops) {
            events[eventCount++] = w;
        }
        for (Conference cf : conferences) {
            events[eventCount++] = cf;
        }

        if (eventCount > 0) {
            System.out.println(eventCount + " event(s) loaded from files.");
        }
    }

    static void appendToConferenceFile(String eventID, String title, LocalDate date, String venue, int maxTickets,
            int sessionCount, Session[] sessions) {
        try {
            File confFile = new File("Conference.json");
            confFile.createNewFile();
            try (Writer writer = new java.io.FileWriter(confFile, true)) {
                writeConferenceRecord(writer, eventID, title, date, venue, maxTickets, sessionCount, sessions);
            }
        } catch (IOException e) {
            System.out.println("Error auto-saving conference data: " + e.getMessage());
        }
    }

    static void updateInFile(Conference conference) {
        try {
            List<Conference> conferences = readConferenceData();
            for (int i = 0; i < conferences.size(); i++) {
                if (conferences.get(i).getEventID().equals(conference.getEventID())) {
                    conferences.set(i, conference);
                    storeConferenceData();

                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating conference data: " + e.getMessage());
        }
    }

    /**
     * Writes a single conference record.
     *
     * Format per record:
     * eventID, title, date, venue, maxTickets, sessionCount
     * then per session: sessionID, topic, time, speakerCount, [username x
     * speakerCount]
     *
     * Session now stores usernames directly (String[]), so No Speaker object is
     * needed.
     */
    private static void writeConferenceRecord(Writer writer, String eventID, String title, LocalDate date, String venue,
            int maxTickets, int sessionCount, Session[] sessions) {
        try {
            writer.write(eventID + "\n");
            writer.write(title + "\n");
            writer.write(date.toString() + "\n");
            writer.write(venue + "\n");
            writer.write(maxTickets + "\n");
            writer.write(sessionCount + "\n");
            for (int s = 0; s < sessionCount; s++) {
                Session session = sessions[s];
                writer.write(session.getSessionID() + "\n");
                writer.write(session.getTopic() + "\n");
                writer.write(session.getTime() + "\n");
                writer.write(session.getSpeakerCount() + "\n");

                // Save speaker usernames
                String[] usernames = session.getSpeakers();
                for (int sp = 0; sp < session.getSpeakerCount(); sp++) {
                    writer.write(usernames[sp] + "\n");
                }

                // NEW: Save speaker status and rejection reason for each speaker
                for (int sp = 0; sp < session.getSpeakerCount(); sp++) {
                    writer.write(session.getSpeakerStatus(usernames[sp]) + "\n");
                    writer.write(session.getRejectionReason(usernames[sp]) + "\n");
                }
            }
        } catch (Exception e) {
            System.out.println("Error writing conference record: " + e.getMessage());
        }
    }

    public void createConferenceFile() {
        try {
            File confFile = new File("Conference.json");
            if (confFile.createNewFile()) {
                System.out.println("Please Waiting...");
                System.out.println("Conference file created: " + confFile.getName());
            }
        } catch (IOException e) {
            System.out.println("Error creating conference file: " + e.getMessage());
        }
    }

    // -- Static file I/O ------------------------------------------------------

    /**
     * Reads all conferences from "Conference.json".
     * Session speaker slots are restored as plain usernames (String) No Speaker
     * object is constructed.
     */
    public static List<Conference> readConferenceData() {
        List<Conference> conferences = new ArrayList<>();
        try {
            File confFile = new File("Conference.json");
            if (!confFile.exists()) {
                // File doesn't exist, create it and return empty list
                confFile.createNewFile();
                return conferences;
            }

            List<String> lines = Files.readAllLines(Paths.get("Conference.json"));

            // If file is empty, return empty list
            if (lines.isEmpty()) {
                return conferences;
            }

            int i = 0;
            while (i < lines.size()) {
                // Make sure we have enough lines before reading
                if (i + 6 > lines.size())
                    break;

                String eventID = lines.get(i++);
                String title = lines.get(i++);
                LocalDate date = LocalDate.parse(lines.get(i++));
                String venue = lines.get(i++);
                int maxTickets = Integer.parseInt(lines.get(i++));
                int storedSessionCount = Integer.parseInt(lines.get(i++));

                Conference conf = new Conference(title, date, venue, maxTickets);
                conf.setEventID(eventID);

                for (int s = 0; s < storedSessionCount; s++) {
                    // Check if we have enough lines for session basic info
                    if (i + 4 > lines.size())
                        break;

                    String sessionID = lines.get(i++);
                    String topic = lines.get(i++);
                    String time = lines.get(i++);
                    int speakerCount = Integer.parseInt(lines.get(i++));
                    Session session = new Session(topic, time);
                    session.setSessionID(sessionID);

                    // Restore speaker usernames
                    for (int sp = 0; sp < speakerCount; sp++) {
                        if (i >= lines.size())
                            break;
                        String username = lines.get(i++);
                        session.addSpeaker(username);
                    }

                    // Restore speaker status and rejection reason (if they exist in file)
                    String[] usernames = session.getSpeakers();
                    for (int sp = 0; sp < speakerCount; sp++) {
                        // Check if we have enough lines for status and reason
                        if (i + 1 >= lines.size())
                            break;
                        String status = lines.get(i++);
                        String reason = lines.get(i++);
                        session.setSpeakerStatusByUsername(usernames[sp], status);
                        session.setRejectionReasonByUsername(usernames[sp], reason);
                    }

                    conf.setSession(session);
                }
                conferences.add(conf);
            }
        } catch (IOException e) {
            System.out.println("Error reading conference data: " + e.getMessage());
        }
        return conferences;
    }

    public static void displayAllConferences(List<Conference> conferences) {
        System.out.println("=== Conference Info ===");
        System.out.printf("%-6s %-20s %-12s %-20s %-8s%n",
                "ID", "Title", "Date", "Venue", "MaxTix");
        System.out.println("--------------------------------------------------------------------");
        for (Conference conf : conferences) {
            System.out.println(conf.toString());
            conf.displaySessions();
        }
    }

    public static void storeConferenceData() {
        try (Writer writer = new java.io.FileWriter("Conference.json")) {
            for (Conference c : conferences) {
                writeConferenceRecord(writer, c.getEventID(), c.getTitle(), c.getDate(), c.getVenue(),
                        c.getMaxTickets(), c.getSessionCount(), c.getSessions());

            }
        } catch (IOException e) {
            System.out.println("Error storing conference data: " + e.getMessage());
        }
    }

    // Concert

    // -- File I/O -------------------------------------------------------------

    // Appends this concert's data to Concert.json
    private static void appendToConcertFile(Concert c) {
        try {
            File concertFile = new File("Concert.json");
            concertFile.createNewFile();
            try (Writer writer = new java.io.FileWriter(concertFile, true)) {

                writeConcertRecord(writer, c);

            }
        } catch (IOException e) {
            System.out.println("Error auto-saving concert data: " + e.getMessage());
        }
    }

    // Helper: writes one concert record
    // Format: eventID / title / date / venue / maxTickets / speakerCount / [name x
    // N]
    public static void writeConcertRecord(Writer writer, Concert c) throws IOException {
        writer.write(c.getEventID() + "\n");
        writer.write(c.getTitle() + "\n");
        writer.write(c.getDate().toString() + "\n");
        writer.write(c.getVenue() + "\n");
        writer.write(c.getMaxTickets() + "\n");
        writer.write(c.getSpeakerCount() + "\n");
        for (int i = 0; i < c.getSpeakerCount(); i++) {
            writer.write(c.getSpeakers()[i] + "\n");
            writer.write(c.getSpeakerStatus(c.getSpeakers()[i]) + "\n");
            writer.write(c.getRejectionReason(c.getSpeakers()[i]) + "\n");
        }
    }

    // create Concert file
    public static void createConcertFile() {
        try {
            File concertFile = new File("Concert.json");
            if (concertFile.createNewFile()) {
                System.out.println("Please Waiting...");
                System.out.println("Concert file created: " + concertFile.getName());
            }
        } catch (IOException e) {
            System.out.println("Error creating concert file: " + e.getMessage());
        }
    }

    /**
     * Reads all concerts from "Concert.json".
     * Format per record:
     * eventID, title, date, venue, maxTickets, speakerCount, [name x speakerCount]
     */
    public static List<Concert> readConcertData() {
        List<Concert> concerts = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("Concert.json"));
            if (!lines.isEmpty()) {
                int i = 0;
                while (i < lines.size()) {
                    String eventID = lines.get(i++);
                    String title = lines.get(i++);
                    LocalDate date = LocalDate.parse(lines.get(i++));
                    String venue = lines.get(i++);
                    int maxTickets = Integer.parseInt(lines.get(i++));
                    int storedSpeakerCount = Integer.parseInt(lines.get(i++));

                    Concert c = new Concert(title, date, venue, maxTickets);
                    c.setEventID(eventID);

                    for (int s = 0; s < storedSpeakerCount; s++) {
                        c.getSpeakers()[s] = lines.get(i++);
                        c.setSpeakerCount();
                        c.setSpeakerStatus(c.getSpeakers()[s], lines.get(i++));
                        c.setRejectionReason(c.getSpeakers()[s], lines.get(i++));
                    }

                    concerts.add(c);

                }
            }
        } catch (IOException e) {
            System.out.println("Error reading concert data: " + e.getMessage());
        }
        return concerts;
    }

    // store concert data to Concert.json
    public static void storeConcertData(List<Concert> concerts) {
        try (Writer writer = new java.io.FileWriter("Concert.json")) {
            for (Concert c : concerts) {
                writeConcertRecord(writer, c);
            }
        } catch (IOException e) {
            System.out.println("Error storing concert data: " + e.getMessage());
        }
    }

    // Workshop

    // -- File I/O -------------------------------------------------------------

    // Appends this workshop's data to Workshop.json
    public static void appendToWorkshopFile(Workshop w) {
        try {
            File workshopFile = new File("Workshop.json");
            workshopFile.createNewFile();
            try (Writer writer = new java.io.FileWriter(workshopFile, true)) {
                writeWorkshopRecord(writer, w);
            }
        } catch (IOException e) {
            System.out.println("Error auto-saving workshop data: " + e.getMessage());
        }
    }

    // Helper: writes one workshop record
    // Format: eventID / title / date / venue / maxTickets / speakerCount / [name x
    // N]
    public static void writeWorkshopRecord(Writer writer, Workshop w) throws IOException {
        writer.write(w.getEventID() + "\n");
        writer.write(w.getTitle() + "\n");
        writer.write(w.getDate().toString() + "\n");
        writer.write(w.getVenue() + "\n");
        writer.write(w.getMaxTickets() + "\n");
        writer.write(w.getSpeakerCount() + "\n");
        for (int i = 0; i < w.getSpeakerCount(); i++) {
            writer.write(w.getSpeakers()[i] + "\n");
            writer.write(w.getRejectionReason(w.getSpeakers()[i]) + "\n");
            writer.write(w.getSpeakerStatus(w.getSpeakers()[i]) + "\n");
        }
    }

    // create Workshop file
    public void createWorkshopFile() {
        try {
            File workshopFile = new File("Workshop.json");
            if (workshopFile.createNewFile()) {
                System.out.println("Please Waiting...");
                System.out.println("Workshop file created: " + workshopFile.getName());
            }
        } catch (IOException e) {
            System.out.println("Error creating workshop file: " + e.getMessage());
        }
    }

    /**
     * Reads all workshops from "Workshop.json".
     * Format per record:
     * eventID, title, date, venue, maxTickets, speakerCount, [name x speakerCount]
     */

    // store workshop data to Workshop.json
    public static void storeWorkshopData(List<Workshop> workshops) {
        try (Writer writer = new java.io.FileWriter("Workshop.json")) {
            for (Workshop w : workshops) {
                writeWorkshopRecord(writer, w);
            }
        } catch (IOException e) {
            System.out.println("Error storing workshop data: " + e.getMessage());
        }
    }

    public static List<Workshop> readWorkshopData() {
        List<Workshop> workshops = new ArrayList<>();
        try {
            File workshopFile = new File("Workshop.json");
            if (!workshopFile.exists()) {
                return workshops;
            }

            List<String> lines = Files.readAllLines(Paths.get("Workshop.json"));
            if (!lines.isEmpty()) {
                int i = 0;
                while (i + 6 < lines.size()) {
                    String eventID = lines.get(i++);
                    String title = lines.get(i++);
                    LocalDate date = LocalDate.parse(lines.get(i++));
                    String venue = lines.get(i++);
                    int maxTickets = Integer.parseInt(lines.get(i++));
                    int storedSpeakerCount = Integer.parseInt(lines.get(i++));

                    Workshop w = new Workshop(title, date, venue, maxTickets);
                    w.setEventID(eventID);

                    for (int s = 0; s < storedSpeakerCount; s++) {
                        String speakerName = lines.get(i++);
                        w.assignSpeaker(speakerName);

                        if (i + 1 < lines.size()) {
                            String nextLine = lines.get(i);
                            // Check if next line is a valid status string
                            if (nextLine != null && (nextLine.equals("pending") ||
                                    nextLine.equals("accepted") || nextLine.equals("rejected"))) {
                                String status = lines.get(i++);
                                String reason = lines.get(i++);
                                w.setSpeakerStatus(speakerName, status);
                                w.setRejectionReason(speakerName, reason);
                            }
                        }
                    }
                    workshops.add(w);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading workshop data: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error parsing workshop data: " + e.getMessage());
        }
        return workshops;
    }

    // -------------------------------------------------------------------------
    // ATTENDEE MENU
    // -------------------------------------------------------------------------
    static void attendeeMenu(Attendee attendee) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--------------------------------");
            System.out.println("|       ATTENDEE MENU          |");
            System.out.println("|------------------------------|");
            System.out.println("|  1: View Events              |");
            System.out.println("|  2: Purchase Ticket          |");
            System.out.println("|  3: View History             |");
            System.out.println("|  0: Back to Main Menu        |");
            System.out.println("--------------------------------");
            System.out.print("Enter option: ");
            int choice = scan.nextInt();
            System.out.println("\n");

            switch (choice) {
                case 1:
                    displayEvents();
                    break;
                case 2:
                    purchaseTicket(attendee);
                    break;
                case 3:
                    attendee.TicketPurchasedHistory(tickets);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // purchase ticket flow
    static void purchaseTicket(Attendee a) {
        if (eventCount == 0) {
            System.out.println("No events available.");
            return;
        }

        // pick event
        String eventId;
        while (true) {
            displayEvents();
            scan.nextLine();
            System.out.print("Enter Event ID: ");
            eventId = scan.nextLine();

            if (ems.validationInputEventId(eventId)) {
                break;
            } else {
                System.out.println("Invalid Event ID. Try again.");
            }
        }

        TicketType tt = TicketType.findTicketTypeById(ticketTypes, eventId);

        if (tt == null) {
            System.out.println("Error: No ticket types configured for this event yet.");
            return;
        }

        // pick ticket type
        String ticketType = "";
        while (true) {
            try {
                System.out.println("===== Ticket Type =====");
                System.out.println("1. EarlyBird  - RM " + tt.getPrice("earlybird") + " ("
                        + tt.getAvailableType("earlybird") + " tickets available)");
                System.out.println("2. Standard   - RM " + tt.getPrice("standard") + " ("
                        + tt.getAvailableType("standard") + " tickets available)");
                System.out.println("3. VIP        - RM " + tt.getPrice("vip") + " (" + tt.getAvailableType("vip")
                        + " tickets available)");
                System.out.print("Select ticket type: ");
                int type = scan.nextInt();
                scan.nextLine();
                if (type == 1) {
                    ticketType = "earlybird";
                } else if (type == 2) {
                    ticketType = "standard";
                } else if (type == 3) {
                    ticketType = "vip";
                } else {
                    System.out.println("Invalid option. Try again.");
                    continue;
                }

                if (!tt.isAvailable(ticketType)) {
                    System.out.println("Sorry, No " + ticketType + " tickets available!");
                    return;
                }
                break;
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number (1, 2, or 3).");
                scan.nextLine(); // Clear the invalid input buffer
            }
        }

        if (!validationPurchaseTicket(tt, ticketType)) {
            System.out.println("Sorry, the ticket type is not available for this period.");
            return;
        }

        // payment
        System.out.println("\nThe total amount = RM " + tt.getPrice(ticketType));
        while (true) {
            try {
                System.out.println("Payment Method");
                System.out.println("1. Touch N Go");
                System.out.println("2. Credit/Debit Card");
                System.out.println("3. Online Banking");
                System.out.print("Select your payment method: ");
                int method = scan.nextInt();
                scan.nextLine();
                if (method == 1 || method == 2 || method == 3) {
                    break;
                } else {
                    System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number (1, 2, or 3).");
                scan.nextLine(); // Clear the invalid input buffer
            }
        }
        System.out.println("\nProcessing payment...");
        System.out.println("Payment Success!");

        Payment p = new Payment(a, eventId, tt.getPrice(ticketType));
        payments[ticketCount] = p;
        System.out.print(p.toString());
        Ticket ticket = ems.purchaseTicket(tt, eventId, ticketType, p, ticketCount);

        if (ticket != null) {
            scan.nextLine();
            System.out.println("\nPurchase completed successfully!");
            scan.nextLine();
            ticket.displayTicketDetails();
            tickets.add(ticket);
            storeTicketData(tickets);
            storeTicketTypeData(ticketTypes); // update available quantity
            scan.nextLine();
        } else {
            System.out.println("Purchase failed. Please try again.");
        }
    }

    static void viewAllTicketType() {
        if (ticketTypes.isEmpty()) {
            System.out.println("No ticket type created.");
        }
        System.out.println("\nAll Ticket Type\n-----------------------------");
        TicketType.displayAllTicketType(ticketTypes);
    }

    static void displayEvents() {
        System.out.println(
                "  -------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("  | %-2s | %-4s | %-12s | %-15s | %-10s | %-15s | %-19s | %-18s | %16s |%n",
                "No", "ID", "Type", "Title", "Date", "Venue", "Sales Start Date", "Sales End Date", "Available Ticket");
        System.out.println(
                "  |----|------|--------------|-----------------|------------|-----------------|---------------------|--------------------|------------------|");
        for (int i = 0; i < eventCount; i++) {
            Event e = events[i];
            String type = e.getClass().getSimpleName();
            String title = e.getTitle().length() > 20 ? e.getTitle().substring(0, 17) + "..." : e.getTitle();
            String venue = e.getVenue().length() > 20 ? e.getVenue().substring(0, 17) + "..." : e.getVenue();
            TicketType tt = TicketType.findTicketTypeById(ticketTypes, e.getEventID());

            System.out.printf("  | %-2d | %-4s | %-12s | %-15s | %-10s | %-15s | %-19s | %-18s | %3d              |%n",
                    (i + 1), e.getEventID(), type, title, e.getDate(), venue, tt.getSalesStart(), tt.getSalesEnd(),
                    tt.getAvailableQuantity());
        }
        System.out.println(
                "  -------------------------------------------------------------------------------------------------------------------------------------------");
    }

    static void UpdateTicketType() {
        if (ticketTypes.isEmpty()) {
            System.out.println("No ticket type created.");
            return;
        }
        viewAllTicketType();
        System.out.print("Enter number of ticket type to update (0 to cancel): ");
        int choice = readInt();
        if (choice == 0) {
            System.out.println("Cancelled.");
            return;
        }
        if (choice < 1 || choice > ticketTypes.size()) {
            System.out.println("Invalid input. Please retry.");
            return;
        }
        System.out.println("Ticket Type " + choice + "\n============================");
        TicketType tt = ticketTypes.get(choice - 1);
        System.out.print(tt.toString());
        System.out.println("\n\n1. Quantity of ticket");
        System.out.println("2. Price of ticket");
        System.out.println("3. Perks");
        System.out.println("4. Sales Start Date");
        System.out.println("5. Sales End Date");
        System.out.println("0. Cancel");
        System.out.print("Select which field you want to update: ");
        int choice1 = readInt();
        if (choice1 == 0) {
            System.out.println("Cancelled.");
            return;
        }
        System.out.print("\nUpdating ticket type......");
        switch (choice1) {
            case 1:
                int maxTix = 0;
                int qeb = 0;
                int qsd = 0;
                int qvip = 0;
                do {
                    try {
                        scan.nextLine();
                        System.out.print("\nMax Ticket (Recommend 150):");
                        maxTix = readInt();
                        System.out.print("Quantity Early Bird (Recommend 20% of total ticket):");
                        qeb = readInt();
                        System.out.print("Quantity Standard (Recommend 60% of total ticket):");
                        qsd = readInt();
                        System.out.print("Quantity Vip (Recommend 20% of total ticket):");
                        qvip = readInt();
                    } catch (Exception e) {
                        System.out.println("Invalid input. Please retry.");
                    }
                } while (!validationQuantityTicket(maxTix, qeb, qsd, qvip));
                tt.setTotalQuantity(maxTix, qeb, qsd, qvip);
                tt.updateQuantityWithSoldTickets(qeb, qsd, qvip);
                break;
            case 2:
                double peb = 0.0;
                double psd = 0.0;
                double pvip = 0.0;
                boolean validPrice = false;
                while (!validPrice) {
                    try {
                        System.out.print("\nPrice Early Bird (RM):");
                        peb = Double.parseDouble(scan.nextLine().trim());
                        System.out.print("Price Standard (RM):");
                        psd = Double.parseDouble(scan.nextLine().trim());
                        System.out.print("Price Vip (RM):");
                        pvip = Double.parseDouble(scan.nextLine().trim());
                        if (validationPrice(peb, psd, pvip))
                            validPrice = true;
                    } catch (Exception e) {
                        System.out.println("Invalid input. Please enter numbers only.");
                    }
                }
                tt.setPrice(peb, psd, pvip);
                break;
            case 3:
                String perks;
                do {
                    System.out.print("\nPerks Provided: (if no just enter -) ");
                    perks = scan.nextLine();
                } while (!validationPerks(perks));
                tt.setPerks(perks);
                break;
            case 4:
                String ssdate;
                LocalDate salesStartDate;
                do {
                    System.out.print("\nSales Start Date (YYYY-MM-DD) : ");
                    ssdate = scan.nextLine();
                    salesStartDate = validationSalesStartDate(ssdate,
                            ems.findEventById(tt.getEventId()).getDate());
                } while (salesStartDate == null);
                tt.setSalesStart(salesStartDate);
                break;
            case 5:
                String sedate;
                LocalDate salesEndDate;
                do {
                    System.out.print("\nSales End Date (YYYY-MM-DD) : ");
                    sedate = scan.nextLine();
                    salesEndDate = validationSalesEndDate(sedate, tt.getSalesStart(),
                            ems.getEventById(tt.getEventId()).getDate());
                } while (salesEndDate == null);
                tt.setSalesEnd(salesEndDate);
                break;
            default:
                System.out.println("Invalid input. Please retry");
        }
        storeTicketTypeData(ticketTypes);
        System.out.println("Update Successfully.");
    }

    // create Ticket file
    public void createTicketFile() {
        try {
            File ticketFile = new File("Ticket.json");
            if (ticketFile.createNewFile()) {
                System.out.println("Please Waiting...");
                System.out.println("Ticket file created: " + ticketFile.getName());
            }
        } catch (IOException e) {
            System.out.println("Error creating ticket file: " + e.getMessage());
        }
    }

    // Reads all ticket from "Ticket.json"
    public static List<Ticket> readTicketFile() {
        List<Ticket> tickets = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("Ticket.json"));
            if (!lines.isEmpty()) {
                int i = 0;
                while (i < lines.size()) {
                    String ticketId = lines.get(i);
                    boolean status = Boolean.parseBoolean(lines.get(i + 1));
                    String buyerName = lines.get(i + 2);
                    String eventId = lines.get(i + 3);
                    String ticketType = lines.get(i + 4);
                    double totalAmount = Double.parseDouble(lines.get(i + 5));
                    String seatNumber = lines.get(i + 6);
                    String perks = lines.get(i + 7);
                    LocalDate purchasedDate = LocalDate.parse(lines.get(i + 8));
                    String bookingId = lines.get(i + 9);

                    Ticket t = new Ticket(ticketId, status, buyerName, eventId, ticketType, totalAmount, seatNumber,
                            perks,
                            purchasedDate, bookingId);
                    tickets.add(t);
                    i += 10; // 10 lines per record
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading ticket data: " + e.getMessage());
        }
        return tickets;
    }

    // store ticket history data to Ticket.json
    public static void storeTicketData(List<Ticket> tickets) {
        try (Writer writer = new java.io.FileWriter("Ticket.json")) {
            for (Ticket t : tickets) {
                writer.write(t.getTicketId() + "\n");
                writer.write(t.getStatus() + "\n");
                writer.write(t.getBuyerName() + "\n");
                writer.write(t.getEventId() + "\n");
                writer.write(t.getTicketType() + "\n");
                writer.write(t.getTotalAmount() + "\n");
                writer.write(t.getSeatNum() + "\n");
                writer.write(t.getPerks() + "\n");
                writer.write(t.getPurchasedDate() + "\n");
                writer.write(t.getBookingId() + "\n");

            }
        } catch (IOException e) {
            System.out.println("Error storing ticket data: " + e.getMessage());
        }
    }

    // create TicketType file
    public static void createTicektTypeFile() {
        try {
            File ttFile = new File("TicketType.json");
            if (ttFile.createNewFile()) {
                System.out.println("Please Waiting...");
                System.out.println("Ticket Type file created: " + ttFile.getName());
            }
        } catch (IOException e) {
            System.out.println("Error creating ticket type file: " + e.getMessage());
        }
    }

    public static boolean validationPurchaseTicket(TicketType tt, String ticketTypeName) {
        if (LocalDate.now().isAfter(tt.getSalesEnd()) || LocalDate.now().isBefore(tt.getSalesStart())) {
            System.out
                    .println("Error: Ticket cannot be purchased because the sales period haven't start/already over!");
            return false;
        } else if (ticketTypeName.toLowerCase().equals("earlybird")) {
            if (LocalDate.now().isAfter(tt.getEarlyBirdEnd())) {
                System.out.println("Error: Early Bird ticket cannot be purchased due to period is over!");
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    // ticket type part
    // validate the quantity set
    public static boolean validationQuantityTicket(int totalQuantity, int quantityEarlyBird, int quantityStandard,
            int quantityVip) {
        if (totalQuantity == quantityEarlyBird + quantityStandard + quantityVip) {
            return true;
        } else {
            System.out.println(
                    "Sum of ticket type quantities not equal to totalQuantity. Please reset the quantity of ticket.");
            return false;
        }
    }

    // validate price
    public static boolean validationPrice(double priceEarlyBird, double priceStandard, double priceVip) {
        if (priceEarlyBird < 0 || priceStandard < 0 || priceVip < 0) {
            System.out.println("Ticket prices cannot be negative.");
            return false;
        }

        if (priceVip <= priceStandard) {
            System.out.println(
                    "VIP price (RM" + priceVip + ") should be greater than Standard price (RM" + priceStandard + ").");
            return false;
        }

        if (priceStandard <= priceEarlyBird) {
            System.out.println("Standard price (RM" + priceStandard + ") should be greater than Early Bird price (RM"
                    + priceEarlyBird + ").");
            return false;
        }

        return true;
    }

    public static boolean validationPerks(String perks) {
        if (perks == null || perks.trim().isEmpty()) {
            System.out.println("Error: Perks cannot be empty !");
            return false;
        } else {
            return true;
        }
    }

    public static LocalDate validationSalesStartDate(String ssdate, LocalDate eventDate) {
        if (ssdate == null || ssdate.trim().isEmpty()) {
            System.out.println("Error: Sales Start Date cannot be empty !");
            return null;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(ssdate.trim());
            if (parsedDate.isAfter(eventDate) || parsedDate.isEqual(eventDate)) {
                System.out.println("Error: Sales Start Date must before event date !");
                return null;
            } else if (parsedDate.isBefore(LocalDate.now())) {
                System.out.println("Error: Sales Start Date must in the future !");
                return null;
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            System.out.println("Error: Date format must be YYYY-MM-DD !");
            return null;
        }
    }

    public static LocalDate validationSalesEndDate(String sedate, LocalDate salesStartDate, LocalDate eventDate) {
        if (sedate == null || sedate.trim().isEmpty()) {
            System.out.println("Error: Sales End Date cannot be empty !");
            return null;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(sedate.trim());
            if (parsedDate.isAfter(eventDate) || parsedDate.isEqual(eventDate)) {
                System.out.println("Error: Sales End Date must before event date !");
                return null;
            } else if (parsedDate.isBefore(salesStartDate) || parsedDate.isEqual(salesStartDate)) {
                System.out.println("Error: Sales End Date must after sales start date !");
                return null;
            } else if (parsedDate.isBefore(LocalDate.now())) {
                System.out.println("Error: Sales End Date must in the future !");
                return null;
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            System.out.println("Error: Date format must be YYYY-MM-DD !");
            return null;
        }
    }

    // Reads all ticket type from "TicketType.json"
    public static List<TicketType> readTicektTypeData() {
        List<TicketType> ticketTypes = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("TicketType.json"));
            if (!lines.isEmpty()) {
                int i = 0;
                while (i < lines.size()) {
                    String eventId = lines.get(i);
                    int totalQuantity = (int) Double.parseDouble(lines.get(i + 1));
                    int quantityEarlyBird = (int) Double.parseDouble(lines.get(i + 2));
                    int quantityStandard = (int) Double.parseDouble(lines.get(i + 3));
                    int quantityVip = (int) Double.parseDouble(lines.get(i + 4));
                    int availableQuantity = (int) Double.parseDouble(lines.get(i + 5));
                    int availableEarlyBird = (int) Double.parseDouble(lines.get(i + 6));
                    int availableStandard = (int) Double.parseDouble(lines.get(i + 7));
                    int availableVip = (int) Double.parseDouble(lines.get(i + 8));
                    double priceEarlyBird = Double.parseDouble(lines.get(i + 9));
                    double priceStandard = Double.parseDouble(lines.get(i + 10));
                    double priceVip = Double.parseDouble(lines.get(i + 11));
                    String perks = lines.get(i + 12);
                    LocalDate salesStart = LocalDate.parse(lines.get(i + 13));
                    LocalDate salesEnd = LocalDate.parse(lines.get(i + 14));
                    // earlyBirdEnd is not stored constructor computes it as
                    // salesStart.plusDays(1)

                    TicketType tt = new TicketType(eventId, totalQuantity, quantityEarlyBird, quantityStandard,
                            quantityVip, availableQuantity, availableEarlyBird, availableStandard, availableVip,
                            priceEarlyBird, priceStandard, priceVip, perks, salesStart, salesEnd);
                    ticketTypes.add(tt);

                    // NEW: Load tickets and remove already sold seats
                    List<Ticket> allTickets = readTicketFile();
                    for (Ticket ticket : allTickets) {
                        if (ticket.getEventId().equals(tt.getEventId())) {
                            // Remove the seat that was already sold
                            String seatToRemove = ticket.getSeatNum();
                            tt.removeSeat(ticket.getTicketType(), seatToRemove);
                        }
                    }

                    i += 15; // 15 lines per record
                }
            } else {
                System.out.println("There is no ticket type record created.");
            }
        } catch (IOException e) {
            System.out.println("Error reading ticket type data: " + e.getMessage());
        }
        return ticketTypes;
    }

    // store ticket type data
    public static void storeTicketTypeData(List<TicketType> TicketTypes) {
        try (Writer writer = new java.io.FileWriter("TicketType.json")) {
            for (TicketType tt : TicketTypes) {
                writer.write(tt.getEventId() + "\n");
                writer.write(tt.getTotalQuantity() + "\n");
                writer.write(tt.getQuantityOfAllTicketType()[0] + "\n");
                writer.write(tt.getQuantityOfAllTicketType()[1] + "\n");
                writer.write(tt.getQuantityOfAllTicketType()[2] + "\n");
                writer.write(tt.getAvailableQuantity() + "\n");
                writer.write(tt.getAvailableType("earlybird") + "\n");
                writer.write(tt.getAvailableType("standard") + "\n");
                writer.write(tt.getAvailableType("vip") + "\n");
                writer.write(String.format("%.2f", tt.getPrice("earlybird")) + "\n");
                writer.write(String.format("%.2f", tt.getPrice("standard")) + "\n");
                writer.write(String.format("%.2f", tt.getPrice("vip")) + "\n");
                writer.write(tt.getPerks() + "\n");
                writer.write(tt.getSalesStart().toString() + "\n");
                writer.write(tt.getSalesEnd().toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error storing ticket type data: " + e.getMessage());
        }
    }

    // speaker part
    static void speakerMenu(Speaker loggedInSpeaker) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n--------------------------------");
            System.out.println("|       SPEAKER MENU           |");
            System.out.println("|  Logged in: " + String.format("%-17s", loggedInSpeaker.getUsername()) + "|");
            System.out.println("|------------------------------|");
            System.out.println("|  1: View & Respond to        |");
            System.out.println("|     Assigned Activities      |");
            System.out.println("|  2: View My Assigned         |");
            System.out.println("|     Activities               |");
            System.out.println("|  3: Update Session Topic     |");
            System.out.println("|  4: Update Bio               |");
            System.out.println("|  5: View My Info             |");
            System.out.println("|  0: Back to Main Menu        |");
            System.out.println("--------------------------------");
            System.out.print("Enter option: ");

            int choice = -1;
            try {
                choice = scan.nextInt();
                scan.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("\nError: Please enter a valid NUMBER (0, 1, 2, 3, 4, or 5)");
                scan.nextLine(); // Clear the invalid input
                continue; // Go back to menu
            }

            switch (choice) {
                case 1:
                    // View assigned sessions and choose to accept/reject
                    manageAssignedSessions(loggedInSpeaker);
                    break;

                case 2:
                    // View all my sessions (with status only)
                    viewMyAssignedSessions(loggedInSpeaker);
                    break;

                case 3:
                    // Update session topic
                    updateSessionTopic(loggedInSpeaker);
                    break;

                case 4:
                    // Update Bio
                    updateSpeakerBio(loggedInSpeaker);
                    break;

                case 5:
                    // View My Info
                    loggedInSpeaker.displaySingleInfo();
                    break;

                case 0:
                    inMenu = false;
                    break;

                default:
                    System.out.println("\n Invalid option. Please enter 0, 1, 2, 3, 4, or 5.");
            }
        }
    }

    // Manage all assigned invitations (accept/reject for all event types)
    static void manageAssignedSessions(Speaker speaker) {
        List<Object> pendingInvitations = new java.util.ArrayList<>();
        List<String> invitationTypes = new java.util.ArrayList<>();

        // Collect all pending invitations from conferences, concerts, and workshops
        for (Event e : events) {
            if (e != null) {
                if (e.isConference()) {
                    Conference conf = (Conference) e;
                    for (int i = 0; i < conf.getSessionCount(); i++) {
                        Session session = conf.getSessions()[i];
                        if (session.hasSpeaker(speaker.getUsername())) {
                            String status = session.getSpeakerStatus(speaker.getUsername());
                            if ("pending".equals(status)) {
                                pendingInvitations.add(session);
                                invitationTypes.add("Conference Session");
                            }
                        }
                    }
                } else if (e.isConcert()) {
                    Concert concert = (Concert) e;
                    if (concert.hasSpeaker(speaker.getUsername())) {
                        String status = concert.getSpeakerStatus(speaker.getUsername());
                        if ("pending".equals(status)) {
                            pendingInvitations.add(concert);
                            invitationTypes.add("Concert");
                        }
                    }
                } else if (e.isWorkshop()) {
                    Workshop workshop = (Workshop) e;
                    if (workshop.hasSpeaker(speaker.getUsername())) {
                        String status = workshop.getSpeakerStatus(speaker.getUsername());
                        if ("pending".equals(status)) {
                            pendingInvitations.add(workshop);
                            invitationTypes.add("Workshop");
                        }
                    }
                }
            }
        }

        if (pendingInvitations.isEmpty()) {
            System.out.println("\nYou have no pending invitations.");
            return;
        }

        boolean continueManaging = true;
        while (continueManaging) {
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("|                         YOUR PENDING INVITATIONS                                 |");
            System.out.println("|----------------------------------------------------------------------------------|");
            System.out.println("| No |    Type      |         Event Name       |            Details                |");
            System.out.println("|----|--------------|--------------------------|-----------------------------------|");

            for (int i = 0; i < pendingInvitations.size(); i++) {
                Object inv = pendingInvitations.get(i);
                String type = invitationTypes.get(i);
                String eventName = "";
                String details = "";

                if (inv instanceof Session) {
                    Session s = (Session) inv;
                    eventName = getConferenceName(s);
                    details = "Topic: " + truncateString(s.getTopic(), 24) + " @ " + s.getTime();
                } else if (inv instanceof Concert) {
                    Concert c = (Concert) inv;
                    eventName = truncateString(c.getTitle(), 24);
                    details = "Date: " + c.getDate() + " | Venue: " + truncateString(c.getVenue(), 20);
                } else if (inv instanceof Workshop) {
                    Workshop w = (Workshop) inv;
                    eventName = truncateString(w.getTitle(), 24);
                    details = "Date: " + w.getDate() + " | Venue: " + truncateString(w.getVenue(), 20);
                }

                System.out.printf("| %-2d | %-12s | %-24s | %-33s |\n",
                        (i + 1), type, eventName, details);
            }

            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("\n0. Back to Main Menu");
            System.out.print("Select invitation number to respond (or 0 to exit): ");

            int choice = -1;
            try {
                choice = scan.nextInt();
                scan.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter a valid NUMBER");
                scan.nextLine();
                continue;
            }

            if (choice == 0) {
                continueManaging = false;
            } else if (choice >= 1 && choice <= pendingInvitations.size()) {
                Object selected = pendingInvitations.get(choice - 1);
 				System.out.println("\n----------------------------------------------------------------------------------");
                System.out.println("|                           INVITATION DETAILS                                    |");
                System.out.println("----------------------------------------------------------------------------------");

                if (selected instanceof Session) {
                    Session session = (Session) selected;
                    System.out.printf("| %-68s |\n", "Type: Conference Session");
                    System.out.printf("| %-68s |\n", "Conference: " + truncateString(getConferenceName(session), 50));
                    System.out.printf("| %-68s |\n", "Session ID: " + session.getSessionID());
                    System.out.printf("| %-68s |\n", "Topic: " + truncateString(session.getTopic(), 50));
                    System.out.printf("| %-68s |\n", "Time: " + session.getTime());
                } else if (selected instanceof Concert) {
                    Concert concert = (Concert) selected;
                    System.out.printf("| %-68s |\n", "Type: Concert");
                    System.out.printf("| %-68s |\n", "Event ID: " + concert.getEventID());
                    System.out.printf("| %-68s |\n", "Title: " + truncateString(concert.getTitle(), 50));
                    System.out.printf("| %-68s |\n", "Date: " + concert.getDate());
                    System.out.printf("| %-68s |\n", "Venue: " + truncateString(concert.getVenue(), 50));
                } else if (selected instanceof Workshop) {
                    Workshop workshop = (Workshop) selected;
                    System.out.printf("| %-68s |\n", "Type: Workshop");
                    System.out.printf("| %-68s |\n", "Event ID: " + workshop.getEventID());
                    System.out.printf("| %-68s |\n", "Title: " + truncateString(workshop.getTitle(), 50));
                    System.out.printf("| %-68s |\n", "Date: " + workshop.getDate());
                    System.out.printf("| %-68s |\n", "Venue: " + truncateString(workshop.getVenue(), 50));
                }

                System.out.println("----------------------------------------------------------------------------------");

                System.out.println("\nDo you want to ACCEPT or REJECT this invitation?");
                System.out.println("1. ACCEPT");
                System.out.println("2. REJECT");
                System.out.print("Enter your choice (1/2): ");

                int response = -1;
                try {
                    response = scan.nextInt();
                    scan.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("Error: Please enter a valid NUMBER (1 for ACCEPT, 2 for REJECT)");
                    scan.nextLine();
                    continue;
                }

                if (response == 1) {
                    if (selected instanceof Session) {
                        ((Session) selected).acceptInvitation(speaker.getUsername());
                        storeConferenceData();
                    } else if (selected instanceof Concert) {
                        ((Concert) selected).acceptInvitation(speaker.getUsername());
                        storeConcertData(concerts);
                    } else if (selected instanceof Workshop) {
                        ((Workshop) selected).acceptInvitation(speaker.getUsername());
                        storeWorkshopData(workshops);
                    }
                    System.out.println("\n You have accepted this invitation!");
                } else if (response == 2) {
                    System.out.print("Please provide a reason for rejection: ");
                    String reason = scan.nextLine();
                    if (selected instanceof Session) {
                        ((Session) selected).rejectInvitation(speaker.getUsername(), reason);
                        storeConferenceData();
                    } else if (selected instanceof Concert) {
                        ((Concert) selected).rejectInvitation(speaker.getUsername(), reason);
                        storeConcertData(concerts);
                    } else if (selected instanceof Workshop) {
                        ((Workshop) selected).rejectInvitation(speaker.getUsername(), reason);
                        storeWorkshopData(workshops);
                    }
              		System.out.println("\n-- You have rejected this invitation.");     
                    System.out.println("Reason: " + reason);
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                    continue;
                }

                System.out.println("\nPress Enter to continue...");
                scan.nextLine();
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    // Helper method to truncate long strings
    private static String truncateString(String str, int maxLength) {
        if (str == null)
            return "";
        if (str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    // View all assigned activities with status
    static void viewMyAssignedSessions(Speaker speaker) {
        System.out.println("\n=== My Assigned Activities ===");
        boolean hasActivities = false;

        // 1. Conference Sessions
        System.out.println("\n--- Conference Sessions ---");
        boolean hasSessions = false;
        for (Event e : events) {
            if (e != null && e.isConference()) {
                Conference conf = (Conference) e;
                for (int i = 0; i < conf.getSessionCount(); i++) {
                    Session session = conf.getSessions()[i];
                    if (session.hasSpeaker(speaker.getUsername())) {
                        hasSessions = true;
                        hasActivities = true;
                        String status = session.getSpeakerStatus(speaker.getUsername());

                        String savedTopic = speaker.getUpdatedSessionTopic(session.getSessionID());
                        if (savedTopic != null) {
                            session.setTopic(savedTopic);
                        }

                        System.out.println("\n  Conference: " + conf.getTitle());
                        System.out.println("    Session ID: " + session.getSessionID());
                        System.out.println("    Topic: " + session.getTopic());
                        System.out.println("    Time: " + session.getTime());
                        System.out.println("    Status: " + status);
                        if ("rejected".equals(status)) {
                            String reason = session.getRejectionReason(speaker.getUsername());
                            if (!reason.isEmpty()) {
                                System.out.println("    Rejection Reason: " + reason);
                            }
                        }
                    }
                }
            }
        }
        if (!hasSessions) {
            System.out.println("  No conference sessions assigned.");
        }

        // 2. Concerts
        System.out.println("\n--- Concerts ---");
        boolean hasConcerts = false;
        for (Event e : events) {
            if (e != null && e.isConcert()) {
                Concert concert = (Concert) e;
                if (concert.hasSpeaker(speaker.getUsername())) {
                    hasConcerts = true;
                    hasActivities = true;
                    String status = concert.getSpeakerStatus(speaker.getUsername());
                    System.out.println("\n  Concert: " + concert.getTitle());
                    System.out.println("    Event ID: " + concert.getEventID());
                    System.out.println("    Date: " + concert.getDate());
                    System.out.println("    Venue: " + concert.getVenue());
                    System.out.println("    Status: " + status);
                    if ("rejected".equals(status)) {
                        String reason = concert.getRejectionReason(speaker.getUsername());
                        if (!reason.isEmpty()) {
                            System.out.println("    Rejection Reason: " + reason);
                        }
                    }
                }
            }
        }
        if (!hasConcerts) {
            System.out.println("  No concerts assigned.");
        }

        // 3. Workshops
        System.out.println("\n--- Workshops ---");
        boolean hasWorkshops = false;
        for (Event e : events) {
            if (e != null && e.isWorkshop()) {
                Workshop workshop = (Workshop) e;
                if (workshop.hasSpeaker(speaker.getUsername())) {
                    hasWorkshops = true;
                    hasActivities = true;
                    String status = workshop.getSpeakerStatus(speaker.getUsername());
                    System.out.println("\n  Workshop: " + workshop.getTitle());
                    System.out.println("    Event ID: " + workshop.getEventID());
                    System.out.println("    Date: " + workshop.getDate());
                    System.out.println("    Venue: " + workshop.getVenue());
                    System.out.println("    Status: " + status);
                    if ("rejected".equals(status)) {
                        String reason = workshop.getRejectionReason(speaker.getUsername());
                        if (!reason.isEmpty()) {
                            System.out.println("    Rejection Reason: " + reason);
                        }
                    }
                }
            }
        }
        if (!hasWorkshops) {
            System.out.println("  No workshops assigned.");
        }

        if (!hasActivities) {
            System.out.println("\nYou are not assigned to any activities.");
        }
    }

    // View editable sessions (accepted sessions only)
    static void viewEditableSessions(Speaker speaker) {
        System.out.println("\n=== Sessions You Can Edit (Accepted) ===");
        boolean hasEditable = false;
        int count = 0;
        for (Event e : events) {
            if (e != null && e.isConference()) {
                Conference conf = (Conference) e;
                for (int i = 0; i < conf.getSessionCount(); i++) {
                    Session session = conf.getSessions()[i];
                    if (session.hasSpeaker(speaker.getUsername())) {
                        String status = session.getSpeakerStatus(speaker.getUsername());
                        if ("accepted".equals(status)) {
                            hasEditable = true;
                            count++;
                            System.out.println("\n" + count + ". Conference: " + conf.getTitle());
                            System.out.println("   Session ID: " + session.getSessionID());
                            System.out.println("   Current Topic: " + session.getTopic());
                            System.out.println("   Time: " + session.getTime());
                        }
                    }
                }
            }
        }

        if (!hasEditable) {
            System.out.println("You have no accepted sessions to edit.");
            System.out.println("Note: You must accept a session first before you can update its topic.");
        }
    }

    // Update session topic
    static void updateSessionTopic(Speaker speaker) {
        viewEditableSessions(speaker);

        System.out.print("\nEnter Session ID to update topic: ");
        String sessionId = scan.nextLine();

        // Find the session
        Session targetSession = null;
        for (Event e : events) {
            if (e != null && e.isConference()) {
                Conference conf = (Conference) e;
                for (int i = 0; i < conf.getSessionCount(); i++) {
                    Session s = conf.getSessions()[i];
                    if (s.getSessionID().equals(sessionId)) {
                        targetSession = s;
                        break;
                    }
                }
            }
            if (targetSession != null)
                break;
        }

        if (targetSession == null) {
            System.out.println("Session not found!");
            return;
        }

        System.out.print("Enter new topic: ");
        String newTopic = scan.nextLine();

        // Update the session topic in memory
        ems.uploadSessionTopic(speaker.getUsername(), targetSession, newTopic);

        // Save the updated topic to speaker.json
        saveSpeakerSessionTopic(speaker.getUsername(), sessionId, newTopic);

        System.out.println("Session topic updated and saved successfully!");
    }

    // Update speaker bio
    static void updateSpeakerBio(Speaker speaker) {
        System.out.println("\n--- Update Your Bio ---");
        System.out.println("Current Bio: " + speaker.getBio());
        System.out.print("Enter new bio: ");
        String newBio = scan.nextLine();

        if (newBio != null && !newBio.trim().isEmpty()) {
            boolean success = speaker.uploadBio(newBio);
            if (success) {
                // Save to speaker.json
                saveSpeakerBio(speaker.getUsername(), newBio);
                System.out.println("Bio updated successfully!");
            } else {
                System.out.println("Failed to update bio.");
            }
        } else {
            System.out.println("Bio cannot be empty. Update cancelled.");
        }
    }

    // Helper method to get conference name
    static String getConferenceName(Session session) {
        for (Event e : events) {
            if (e != null && e.isConference()) {
                Conference conf = (Conference) e;
                for (int i = 0; i < conf.getSessionCount(); i++) {
                    if (conf.getSessions()[i] == session) {
                        return conf.getTitle();
                    }
                }
            }
        }

        return "Unknown Conference";
    }

    // Display single speaker info
    static void displaySpeakerInfo(Speaker speaker) {
        System.out.println("\n=== Speaker Info ===");
        System.out.println("Username: " + speaker.getUsername());
        System.out.println("Email: " + speaker.getEmail());
        System.out.println("Bio: " + speaker.getBio());
    }

}
