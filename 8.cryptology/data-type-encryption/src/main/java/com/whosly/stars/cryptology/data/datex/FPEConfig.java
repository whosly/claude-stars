package com.whosly.stars.cryptology.data.datex;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

/**
 * FPE 配置类
 *
 * @author fengyang
 * @date 2025-10-27 17:02:43
 */
public class FPEConfig {
    private final byte[] key;
    private final LocalDate minDate;
    private final LocalDate maxDate;

    private FPEConfig(byte[] key, LocalDate minDate, LocalDate maxDate) {
        this.key = key;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public static class Builder {
        private byte[] key;
        private LocalDate minDate = LocalDate.of(1970, 1, 1);
        private LocalDate maxDate = LocalDate.of(2070, 12, 31);

        public Builder key(byte[] key) {
            this.key = key;
            return this;
        }

        public Builder keyFromBase64(String base64Key) {
            this.key = Base64.getDecoder().decode(base64Key);
            return this;
        }

        public Builder dateRange(LocalDate minDate, LocalDate maxDate) {
            this.minDate = minDate;
            this.maxDate = maxDate;
            return this;
        }

        public Builder dateRange(Date minDate, Date maxDate) {
            this.minDate = minDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            this.maxDate = maxDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return this;
        }

        public FPEConfig build() {
            if (key == null) {
                throw new IllegalStateException("Key must be set");
            }
            return new FPEConfig(key, minDate, maxDate);
        }
    }

    public ILocalDateFPE createFPE() {
        return new DefaultDateFPEImpl(key, minDate, maxDate);
    }

    // Getters
    public byte[] getKey() { return key.clone(); }
    public LocalDate getMinDate() { return minDate; }
    public LocalDate getMaxDate() { return maxDate; }
}
