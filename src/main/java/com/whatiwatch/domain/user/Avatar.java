package com.whatiwatch.domain.user;

/** 
 * The fixed set of pre-set profile pictures a user can choose.
 */
public enum Avatar {
    AVATAR_1,
    AVATAR_2,
    AVATAR_3,
    AVATAR_4,
    AVATAR_5,
    AVATAR_6;

    // True if the given id names a valid avatar
    public static boolean isValid(String id) {
        if (id == null) {
            return false;
        }
        for (Avatar a : values()) {
            if (a.name().equals(id)) {
                return true;
            }
        }

        return false;
    }
}
