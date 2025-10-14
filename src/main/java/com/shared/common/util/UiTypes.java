package com.shared.common.util;

/**
 * Constants for UI types used in endpoint metadata
 * These help the frontend understand how to render and interact with different endpoints
 */
public class UiTypes {

    // Action types - typically triggered by buttons
    public static final String ACTION = "ACTION";           // Generic action button
    public static final String UPLOAD = "UPLOAD";           // File upload actions
    public static final String PROCESS = "PROCESS";         // Data processing actions
    public static final String VALIDATE = "VALIDATE";       // Validation actions
    public static final String DELETE = "DELETE";           // Delete operations
    public static final String EXPORT = "EXPORT";           // Export data actions

    // Data retrieval types - typically for displaying lists/tables
    public static final String LIST = "LIST";               // Paginated list data
    public static final String DETAIL = "DETAIL";           // Single item details
    public static final String SUMMARY = "SUMMARY";         // Summary/aggregate data
    public static final String SEARCH = "SEARCH";           // Search/filter results

    // Form types - typically for data entry
    public static final String FORM = "FORM";               // Data entry forms
    public static final String CREATE = "CREATE";           // Create new item
    public static final String UPDATE = "UPDATE";           // Update existing item

    // Navigation types
    public static final String NAVIGATION = "NAVIGATION";   // Navigation/menu items

    // Utility types
    public static final String META = "META";               // Metadata/endpoints discovery
    public static final String HEALTH = "HEALTH";           // Health checks
    public static final String CONFIG = "CONFIG";           // Configuration endpoints

    /**
     * Check if the UI type represents an action (button click)
     */
    public static boolean isActionType(String uiType) {
        return ACTION.equals(uiType) ||
               UPLOAD.equals(uiType) ||
               PROCESS.equals(uiType) ||
               VALIDATE.equals(uiType) ||
               DELETE.equals(uiType) ||
               EXPORT.equals(uiType) ||
               CREATE.equals(uiType) ||
               UPDATE.equals(uiType);
    }

    /**
     * Check if the UI type represents data display
     */
    public static boolean isDataDisplayType(String uiType) {
        return LIST.equals(uiType) ||
               DETAIL.equals(uiType) ||
               SUMMARY.equals(uiType) ||
               SEARCH.equals(uiType);
    }

    /**
     * Check if the UI type represents a form
     */
    public static boolean isFormType(String uiType) {
        return FORM.equals(uiType) ||
               CREATE.equals(uiType) ||
               UPDATE.equals(uiType);
    }
}
