package com.hrms.graphql.input;

import com.hrms.enums.PunchSource;
import com.hrms.enums.PunchType;
import com.hrms.enums.VerifyMode;
import lombok.Data;

/**
 * GraphQL input for recording PunchLog.
 */
@Data
public class PunchLogInput {
    private Long employeeId;
    private String biometricId;
    private String deviceId;
    private String deviceName;
    private String location;
    private String punchTime;
    private PunchType punchType;
    private VerifyMode verifyMode;
    private PunchSource punchSource;
}
