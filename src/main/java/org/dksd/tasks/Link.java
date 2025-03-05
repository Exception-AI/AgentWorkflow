package org.dksd.tasks;

public class Link {

    private long left;
    private long right;
    private LinkType linkType;

    public Link(long left, LinkType linkType, long right) {
        this.left = left;
        this.right = right;
        this.linkType = linkType;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(long left) {
        this.left = left;
    }

    public long getRight() {
        return right;
    }

    public void setRight(long right) {
        this.right = right;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }
}
