package com.cringe_studios.cringe_authenticator.util;

import android.net.Uri;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;

public class OTPParser {

    public static OTPData parse(Uri uri) throws IllegalArgumentException {
        if(!"otpauth".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Wrong URI scheme");
        }

        String type = uri.getHost();
        String accountName = uri.getPath();
        String secret = uri.getQueryParameter("secret");
        String algorithm = uri.getQueryParameter("algorithm");
        String digits = uri.getQueryParameter("digits");
        String period = uri.getQueryParameter("period");
        String counter = uri.getQueryParameter("counter");
        String checksum = uri.getQueryParameter("checksum");

        if(type == null || secret == null) {
            throw new IllegalArgumentException("Missing params");
        }

        OTPType fType;
        try {
            fType = OTPType.valueOf(type.toUpperCase());
        }catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse OTP parameters");
        }

        if(fType == OTPType.HOTP && counter == null) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        if(accountName == null || accountName.length() < 2 /* Because path is /accName, so 2 letters for acc with 1 letter name */) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        accountName = accountName.substring(1);

        try {
            // 0 or null for defaults (handled by Cringe-Authenticator-Library)
            OTPAlgorithm fAlgorithm = algorithm == null ? null : OTPAlgorithm.valueOf(algorithm.toUpperCase());
            int fDigits = digits == null ? 0 : Integer.parseInt(digits);
            int fPeriod = period == null ? 0 : Integer.parseInt(period);
            int fCounter = counter == null ? 0 : Integer.parseInt(counter);
            boolean fChecksum = false;
            if(checksum != null) {
                switch(checksum) {
                    case "true": fChecksum = true; break;
                    case "false": break;
                    default: throw new IllegalArgumentException("Checksum must be set to 'true' or 'false'");
                }
            }

            OTPData data = new OTPData(accountName, fType, secret, fAlgorithm, fDigits, fPeriod, fCounter, fChecksum);

            String errorMessage = data.validate();
            if(errorMessage != null) {
                throw new IllegalArgumentException(errorMessage);
            }

            return data;
        }catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse OTP parameters", e);
        }
    }

}
