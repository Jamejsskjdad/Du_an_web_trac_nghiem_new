package com.thanhtam.backend.ultilities;

public enum EQTypeCode {
    /**
     * TF: true/false
     * MC: Multiple choice
     * MS: Multiple select
     * SA: Short answer
     */

    TF("TF"),
    MC("MC"),
    MS("MS"),
    SA("SA"); // Thêm loại câu hỏi trả lời ngắn

    private final String type;

    private EQTypeCode(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
