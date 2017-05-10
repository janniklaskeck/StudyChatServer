package stud.mi.util;

import java.util.Comparator;

import stud.mi.server.Message;

public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(final Message msg1, final Message msg2) {
        if (msg1.getDateSent() < msg2.getDateSent()) {
            return -1;
        }
        if (msg1.getDateSent() > msg2.getDateSent()) {
            return 1;
        }
        return 0;
    }

}
