package com.sicredi.voting.dtos;

public record AgendaResultDto(long yesCount, long noCount, String result) {

    public static AgendaResultDto calculate(long yesCount, long noCount) {
        String result;
        if (yesCount > noCount) {
            result = "APPROVED";
        } else if (noCount > yesCount) {
            result = "REJECTED";
        } else {
            result = "TIED";
        }
        return new AgendaResultDto(yesCount, noCount, result);
    }
}
