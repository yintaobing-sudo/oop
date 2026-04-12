public class Session {
    private String sessionID;
    private String topic;
    private String time;

    // Speaker usernames only — no dependency on Speaker class
    private static final int MAX_SPEAKERS = 5;
    private String[] speakerUsernames = new String[MAX_SPEAKERS];
    private int speakerCount = 0;

    // Speaker status tracking (indexed in parallel with speakerUsernames)
    private String[] speakerStatus = new String[MAX_SPEAKERS];
    private String[] rejectionReason = new String[MAX_SPEAKERS];

    // Auto-generate sessionID: S001, S002, …
    private static int sessionCounter = 1;

    private static String generateSessionID() {
        return String.format("S%03d", sessionCounter++);
    }

    public Session(String topic, String time) {
        this.sessionID = generateSessionID();
        this.topic = topic;
        this.time = time;

        for (int i = 0; i < MAX_SPEAKERS; i++) {
            speakerStatus[i] = "pending";
            rejectionReason[i] = "no";
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getSessionID() {
        return sessionID;
    }

    public String getTopic() {
        return topic;
    }

    public String getTime() {
        return time;
    }

    /**
     * Parses the time field stored as "HHMM" (e.g. "0900", "1430") and returns
     * the total number of minutes since midnight.
     * Returns -1 if the value cannot be parsed so callers can skip the check.
     */
    public int getTimeInMinutes() {
        try {
            String t = this.time.trim();
            if (t.length() != 4)
                return -1;
            int hours = Integer.parseInt(t.substring(0, 2));
            int minutes = Integer.parseInt(t.substring(2, 4));
            return hours * 60 + minutes;
        } catch (Exception e) {
            return -1;
        }
    }

    public int getSpeakerCount() {
        return speakerCount;
    }

    /**
     * Returns the raw username array (length MAX_SPEAKERS; only indices
     * 0..speakerCount-1 are populated).
     */
    public String[] getSpeakers() {
        return speakerUsernames;
    }
    // ── Setters ──────────────────────────────────────────────────────────────

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // Set speaker status by username
public void setSpeakerStatusByUsername(String username, String status) {
    for (int i = 0; i < speakerCount; i++) {
        if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
            speakerStatus[i] = status;
            return;
        }
    }
}

// Set rejection reason by username
public void setRejectionReasonByUsername(String username, String reason) {
    for (int i = 0; i < speakerCount; i++) {
        if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
            rejectionReason[i] = reason;
            return;
        }
    }
}

    // ── Speaker management ───────────────────────────────────────────────────

    /** Add a speaker by username. Returns false if full or already assigned. */
    public boolean addSpeaker(String username) {
        if (speakerCount >= MAX_SPEAKERS) {
            System.out.println("Error: Session [" + sessionID
                    + "] already has the maximum number of speakers.");
            return false;
        }
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i].equals(username)) {
                System.out.println("Error: Speaker [" + username
                        + "] is already assigned to session [" + sessionID + "].");
                return false;
            }
        }
        speakerUsernames[speakerCount] = username;
        speakerStatus[speakerCount] = "pending";
        rejectionReason[speakerCount] = "no";
        speakerCount++;
        return true;
    }

    /** Remove a speaker by username. Returns false if no found. */
    public boolean removeSpeaker(String username) {
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i].equals(username)) {
                // Shift remaining entries left
                for (int j = i; j < speakerCount - 1; j++) {
                    speakerUsernames[j] = speakerUsernames[j + 1];
                    speakerStatus[j] = speakerStatus[j + 1];
                    rejectionReason[j] = rejectionReason[j + 1];
                }
                speakerUsernames[speakerCount - 1] = null;
                speakerStatus[speakerCount - 1] = "pending";
                rejectionReason[speakerCount - 1] = "no";
                speakerCount--;
                System.out.println("Speaker [" + username
                        + "] removed from session [" + sessionID + "].");
                return true;
            }
        }
        System.out.println("Error: Speaker [" + username
                + "] No found in session [" + sessionID + "].");
        return false;
    }

    /** Check whether a username is assigned to this session. */
    public boolean hasSpeaker(String username) {
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
                return true;
            }
        }
        return false;
    }

    /** Get the invitation status for a speaker username. */
    public String getSpeakerStatus(String username) {
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
                return speakerStatus[i] != null ? speakerStatus[i] : "pending";
            }
        }
        return "Not_assigned";
    }

    /** Get the rejection reason for a speaker username. */
    public String getRejectionReason(String username) {
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
                return rejectionReason[i] != null ? rejectionReason[i] : "no";
            }
        }
        return "no";
    }

    /** Accept the session invitation for a given username. */
    public boolean acceptInvitation(String username) {
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
                if ("pending".equals(speakerStatus[i])) {
                    speakerStatus[i] = "accepted";
                    System.out.println("Speaker [" + username
                            + "] accepted session [" + sessionID + "].");
                    return true;
                } else if ("accepted".equals(speakerStatus[i])) {
                    System.out.println("Speaker [" + username
                            + "] has already accepted this session.");
                    return false;
                } else {
                    System.out.println("Speaker [" + username
                            + "] has already rejected this session.");
                    return false;
                }
            }
        }
        System.out.println("Speaker [" + username
                + "] No found in session [" + sessionID + "].");
        return false;
    }

    /** Reject the session invitation for a given username, with a reason. */
    public boolean rejectInvitation(String username, String reason) {
        for (int i = 0; i < speakerCount; i++) {
            if (speakerUsernames[i] != null && speakerUsernames[i].equals(username)) {
                if ("pending".equals(speakerStatus[i])) {
                    speakerStatus[i] = "rejected";
                    rejectionReason[i] = reason;
                    System.out.println("Speaker [" + username
                            + "] rejected session [" + sessionID + "].");
                    System.out.println("Reason: " + reason);
                    return true;
                } else if ("accepted".equals(speakerStatus[i])) {
                    System.out.println("Speaker [" + username
                            + "] has already accepted this session.");
                    return false;
                } else {
                    System.out.println("Speaker [" + username
                            + "] has already rejected this session.");
                    return false;
                }
            }
        }
        System.out.println("Speaker [" + username
                + "] Not found in session [" + sessionID + "].");
        return false;
    }

    // ── toString / equals ────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(sessionID).append("] Topic: ").append(topic)
                .append("  Time: ").append(time).append("  Speakers: ");
        if (speakerCount == 0) {
            sb.append("No");
        } else {
            for (int i = 0; i < speakerCount; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(speakerUsernames[i]);
            }
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null)
            return false;
        if (o instanceof Session) {
            return this.sessionID.equals(((Session) o).getSessionID());
        }
        return false;
    }

    public boolean hasSession(String sessionID) {
        return this.sessionID.equals(sessionID);
    }
}
