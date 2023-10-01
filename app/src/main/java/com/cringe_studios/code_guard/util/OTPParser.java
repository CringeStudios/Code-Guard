package com.cringe_studios.code_guard.util;

import android.net.Uri;
import android.util.Base64;

import com.cringe_studios.code_guard.model.OTPData;
import com.cringe_studios.code_guard.model.OTPMigrationPart;
import com.cringe_studios.code_guard.proto.OTPMigration;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;
import com.cringe_studios.cringe_authenticator_library.impl.Base32;
import com.google.protobuf.InvalidProtocolBufferException;

public class OTPParser {

    public static OTPMigrationPart parseMigration(Uri uri) throws IllegalArgumentException {
        if(!"otpauth-migration".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Wrong URI scheme");
        }

        if(!uri.isHierarchical()) {
            throw new IllegalArgumentException("Not a hierarchical URI");
        }

        String data = uri.getQueryParameter("data");
        if(data == null) {
            throw new IllegalArgumentException("Missing data");
        }

        byte[] dataBytes = Base64.decode(data, Base64.DEFAULT);

        try {
            OTPMigration.MigrationPayload payload = OTPMigration.MigrationPayload.parseFrom(dataBytes);

            int count = payload.getOtpParametersCount();
            OTPData[] otps = new OTPData[count];

            for(int i = 0; i < payload.getOtpParametersCount(); i++) {
                OTPMigration.MigrationPayload.OtpParameters params = payload.getOtpParameters(i);

                String name = params.getName();
                String issuer = params.getIssuer();

                if(name.contains(":")) { // Name possibly contains issuer prefix
                    String[] spl = name.split(":");
                    if(spl.length == 2) { // Otherwise it's not a valid prefix, or too many ':'s, just use everything as the account name
                        if(issuer == null || issuer.isEmpty()) issuer = spl[0];
                        name = spl[1];
                    }
                }

                OTPType type;
                switch(params.getType()) {
                    case OTP_TYPE_UNSPECIFIED:
                    case UNRECOGNIZED:
                    default:
                        // TODO: be more lenient and only exclude the broken codes
                        throw new IllegalArgumentException("Unknown OTP type in migration");
                    case OTP_TYPE_HOTP:
                        type = OTPType.HOTP;
                        break;
                    case OTP_TYPE_TOTP:
                        type = OTPType.TOTP;
                        break;
                }

                String secret = Base32.encode(params.getSecret().toByteArray());

                OTPAlgorithm algorithm;
                switch(params.getAlgorithm()) {
                    case ALGORITHM_UNSPECIFIED:
                    case UNRECOGNIZED:
                    default:
                        throw new IllegalArgumentException("Unknown or unsupported algorithm in migration");
                    case ALGORITHM_SHA1:
                        algorithm = OTPAlgorithm.SHA1;
                        break;
                    case ALGORITHM_SHA256:
                        algorithm = OTPAlgorithm.SHA256;
                        break;
                    case ALGORITHM_SHA512:
                        algorithm = OTPAlgorithm.SHA512;
                        break;
                    case ALGORITHM_MD5:
                        algorithm = OTPAlgorithm.MD5;
                        break;
                }

                int digits;
                switch(params.getDigits()) {
                    case DIGIT_COUNT_UNSPECIFIED:
                    case UNRECOGNIZED:
                    default:
                        throw new IllegalArgumentException("Unknown or unsupported digit count in migration");
                    case DIGIT_COUNT_SIX:
                        digits = 6;
                        break;
                    case DIGIT_COUNT_EIGHT:
                        digits = 8;
                        break;
                }

                int period = 30; // Google authenticator doesn't support other periods
                long counter = params.getCounter();
                boolean checksum = false; // Google authenticator doesn't support checksums

                otps[i] = new OTPData(name, issuer, type, secret, algorithm, digits, period, counter, checksum);
            }

            return new OTPMigrationPart(otps, payload.getBatchIndex(), payload.getBatchSize());
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Failed to parse migration data", e);
        }
    }

    public static OTPData parse(Uri uri) throws IllegalArgumentException {
        if(!"otpauth".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Wrong URI scheme");
        }

        if(!uri.isHierarchical()) {
            throw new IllegalArgumentException("Not a hierarchical URI");
        }

        String type = uri.getHost();
        String path = uri.getPath();
        if(path == null || path.length() < 2) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        path = path.substring(1);

        String issuer = uri.getQueryParameter("issuer");

        String accountName;
        if(path.contains(":")) { // Possibly contains issuer prefix
            String[] spl = path.split(":");
            if(spl.length != 2) { // Either not a valid prefix, or too many ':'s, just use everything as the account name
                accountName = path;
            }else {
                if(issuer == null || issuer.isEmpty()) issuer = spl[0];
                accountName = spl[1];
            }
        }else {
            accountName = path;
        }

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

        if(accountName.length() == 0 || (issuer != null && issuer.length() == 0)) {
            throw new IllegalArgumentException("Missing required parameters");
        }

        try {
            OTPAlgorithm fAlgorithm = algorithm == null ? OTPAlgorithm.SHA1 : OTPAlgorithm.valueOf(algorithm.toUpperCase());
            int fDigits = digits == null ? 6 : Integer.parseInt(digits);
            int fPeriod = period == null ? 30 : Integer.parseInt(period);
            int fCounter = counter == null ? 0 : Integer.parseInt(counter);
            boolean fChecksum = false;
            if(checksum != null) {
                switch(checksum) {
                    case "true": fChecksum = true; break;
                    case "false": break;
                    default: throw new IllegalArgumentException("Checksum must be set to 'true' or 'false'");
                }
            }

            OTPData data = new OTPData(accountName, issuer, fType, secret, fAlgorithm, fDigits, fPeriod, fCounter, fChecksum);

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
