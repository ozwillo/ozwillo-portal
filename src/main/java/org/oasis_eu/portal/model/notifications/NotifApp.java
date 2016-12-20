package org.oasis_eu.portal.model.notifications;

/**
 * User: schambon
 * Date: 4/15/15
 */
public class NotifApp implements Comparable<NotifApp> {
    private String id;
    private String name;

    public NotifApp(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotifApp notifApp = (NotifApp) o;

        return id.equals(notifApp.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(NotifApp o) {
        return name.compareTo(o.getName());
    }
}
