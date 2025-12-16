package com.robsonbs.view;

public record BreadcrumbItem(String label, String href) {

    public static BreadcrumbItem link(String label, String href) {
        return new BreadcrumbItem(label, href);
    }

    public static BreadcrumbItem current(String label) {
        return new BreadcrumbItem(label, null);
    }

    public boolean hasHref() {
        return href != null && !href.isBlank();
    }
}
