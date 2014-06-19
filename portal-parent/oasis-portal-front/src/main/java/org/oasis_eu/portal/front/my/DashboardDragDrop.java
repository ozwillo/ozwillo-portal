package org.oasis_eu.portal.front.my;

/**
 * User: schambon
 * Date: 6/19/14
 */
public class DashboardDragDrop {
    private String contextId;
    private String draggedId;
    private String destId;
    private boolean before;

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getDraggedId() {
        return draggedId;
    }

    public void setDraggedId(String draggedId) {
        this.draggedId = draggedId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public boolean isBefore() {
        return before;
    }

    public void setBefore(boolean before) {
        this.before = before;
    }
}
