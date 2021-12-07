//Hub Roles
export const AUTHOR = 'eh-author'
export const MANAGER = 'eh-manager'
export const ADMIN = 'eh-admin'

//Bundle Status
export const BUNDLE_STATUS = {
    NOT_PUBLISHED: 'NOT_PUBLISHED',
    PUBLISHED: 'PUBLISHED',
    PUBLISH_REQ: 'PUBLISH_REQ',
    DELETE_REQ: 'DELETE_REQ'
}

// HTTP Status
export const HTTP_STATUS = {
    EXPECTATION_FAILED: '417'
}

// All Button Labels
export const BUTTON_LABELS = {
    DELETE: "Delete",
    EDIT: "Edit",
    CANCEL: "Cancel",
    REMOVE: "Remove",
}

// All Modal Labels
export const MODAL_LABELS = {
    DELETE_BUNDLE_MSG: "Are you sure you want to delete this bundle?",
    REMOVE_USER_FROM_ORG_MSG: "Are you sure you want to remove this User from the Organization?",
}

// All API Response Key
export const API_RESPONSE_KEY = {
    EDITED_BUNDLE_GROUP: 'editedBundleGroup'
}

// Constant String
export const DELETED_BUNDLE = 'deletedBundle';
export const GIT_REPO = 'gitRepo';

// REGEX
export const DOCUMENTATION_ADDRESS_URL_REGEX = /[-a-zA-Z0-9@:%_+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_+.~#?&//=]*)?/gi
export const VERSON_REGEX = /^[v]?([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/gm
export const BUNDLE_URL_REGEX = /^(https|git)(:\/\/|@)([^/:]+)[/:]([^/:]+)\/([a-z-A-Z-0-9/]+)(?:\.git)$/gm

// Input char length
export const CHAR_LENGTH = 3;
export const MAX_CHAR_LENGTH = 25;
export const MAX_CHAR_LENGTH_FOR_DESC = 600;
export const MAX_CHAR_LENGTH_FOR_DESC_CATEGORY_AND_ORG_FORM = 100;

/**
 * Bundle Form Validatin Error Message
 */
// NAME
export const NAME_REQ_MSG = 'Name is a required field'
export const LEAST_CHAR_NAME_MSG = `Name must be at least ${CHAR_LENGTH} characters`
export const MAX_CHAR_NAME_MSG = `Name must not exceed ${MAX_CHAR_LENGTH} characters`
// DESCRIPTION
export const DESC_REQ_MESG = 'Description is a required field'
export const LEAST_CHAR_DESC_MSG = `Description must be at least ${CHAR_LENGTH} characters`
export const MAX_CHAR_DESC_MSG = `Description must not exceed ${MAX_CHAR_LENGTH_FOR_DESC} characters`
// Documentation
export const DOCUMENTATION_URL_REQ_MSG = 'Documentation is a required field'
export const DOCUMENTATION_URL_FORMAT_MSG = 'Documentation must match URL format'
// Version
export const VERSION_REQ_MSG = 'Version is a required field'
export const VERSION_FORMAT_MSG = 'Version must match semantic versioning format (e.g. vx.x.x or x.x.x)'

/**
 * Bundle Form Validatin Error Message
 */
// DESCRIPTION ERROR MESSAGE FOR CATEGORY AND ORGANISATION.
export const DESCRIPTION_MAX_LENGTH = 'Description must not exceed 100 characters';

/**
 * Messages
 */
export const MESSAGES = {
    NOTIFY_GUEST_PORTAL_USER_MSG: 'Your account does not currently have access to the Hub. Please contact your Administrator to request access.',
    IMPOSSIBLE_TO_REMOVE_USERS_MSG: 'Impossible to remove the user',
    USER_REMOVED_FROM_ORG_MSG: 'User removed from the organisation'
}

// All dropdown options
export const DROPDOWN_OPTIONS = {
    EDIT: "Edit",
    REMOVE: "Remove",
}
