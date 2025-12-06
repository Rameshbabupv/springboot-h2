package com.hrms.enums;

/**
 * Holiday type classification (Indian statutory).
 * A = FIXED_MANDATORY    - Fixed date, mandatory (Republic Day, Independence Day, Gandhi Jayanti)
 * B = FLOATING_MANDATORY - Date changes yearly (lunar), mandatory (Holi, Eid, Diwali, Dussehra)
 * C = REGIONAL           - State-specific mandatory (Pongal, Onam, Bihu)
 * D = OPTIONAL           - Employee can choose (Christmas, Good Friday, Guru Nanak Jayanti)
 */
public enum HolidayType {
    A,
    B,
    C,
    D
}
