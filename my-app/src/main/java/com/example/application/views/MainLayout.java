package com.example.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.List;

/**
 * Main layout for the Basketball Stat Tracker application.
 * Provides the navigation drawer and header with basketball-themed icons.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        // Updated application name to Basketball Stat Tracker
        Span appName = new Span("Basketball Stat Tracker");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            // Assign basketball-themed icons based on the menu title
            Icon icon = getIconForMenuItem(entry.title());
            nav.addItem(new SideNavItem(entry.title(), entry.path(), icon));
        });

        return nav;
    }

    /**
     * Returns appropriate basketball-themed icons for each menu item.
     * This creates a cohesive sports application feel throughout the UI.
     */
    private Icon getIconForMenuItem(String title) {
        // Map menu items to appropriate basketball/sports themed icons
        return switch (title.toLowerCase()) {
            case "team roster", "home" -> VaadinIcon.USERS.create();
            case "basketball", "stats" -> VaadinIcon.CHART_LINE.create();
            case "skills" -> VaadinIcon.TROPHY.create();
            default -> VaadinIcon.CIRCLE.create();
        };
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}