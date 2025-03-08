package org.dksd.tasks;

import org.dksd.tasks.model.LinkType;

import java.util.Objects;
import java.util.UUID;

public class Link {

    private UUID left;
    private LinkType linkType;
    private UUID right;

    public Link() {

    }

    public Link(UUID left, LinkType linkType, UUID right) {
        this.left = left;
        this.right = right;
        this.linkType = linkType;
    }

    public UUID getLeft() {
        return left;
    }

    public void setLeft(UUID left) {
        this.left = left;
    }

    public UUID getRight() {
        return right;
    }

    public void setRight(UUID right) {
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
