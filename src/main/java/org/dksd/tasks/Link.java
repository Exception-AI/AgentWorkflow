package org.dksd.tasks;

import org.dksd.tasks.model.LinkType;

import java.util.Objects;

public class Link {

    private long left;
    private long right;
    private LinkType linkType;

    public Link() {

    }

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

    @Override
    public String toString() {
        return "Link{" +
                "left=" + left +
                ", right=" + right +
                ", linkType=" + linkType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return left == link.left && right == link.right && linkType == link.linkType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, linkType);
    }
}
