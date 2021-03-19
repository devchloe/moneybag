package com.kakaopay.moneydistribution.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MoneyBagTokenGenerator implements TokenGenerator {
    public String createToken(int digit) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < digit) {
            switch (getRandomNumber(3)) {
                case 0:
                    sb.append(getRandomNumber(10));
                    break;
                case 1:
                    sb.append(getRandomUppercaseLetter());
                    break;
                default:
                    sb.append(getRandomLowercaseLetter());
                    break;
            }
        }
        return sb.toString();
    }

    private static int getRandomNumber(int bound) {
        return getRandom().nextInt(bound);
    }

    private static char getRandomLowercaseLetter() {
        return getCharacter(26, 97);
    }

    private static char getRandomUppercaseLetter() {
        return getCharacter(26, 65);
    }

    private static char getCharacter(int bound, int startAsciiCode) {
        return (char) (getRandom().nextInt(bound) + startAsciiCode);
    }

    private static Random getRandom() {
        return new Random();
    }
}
