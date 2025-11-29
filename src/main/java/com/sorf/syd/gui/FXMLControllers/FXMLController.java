package com.sorf.syd.gui.FXMLControllers;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

public abstract class FXMLController {


    protected static void copyToClip(String str) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(str);
        clipboard.setContent(clipboardContent);
    }

    public abstract void initialize();

    protected static void applyColorAdjust(@NotNull ColorAdjust effect, @NotNull Color targetColor) {
        double hue = map( (targetColor.getHue() + 180) % 360, 0, 360, -1, 1);
        effect.setHue(hue);

        double saturation = targetColor.getSaturation();
        effect.setSaturation(saturation);

        double brightness = map( targetColor.getBrightness(), 0, 1, -1, 0);
        effect.setBrightness(brightness);
    }

    private static double map(double value, double start, double stop, double targetStart, double targetStop) {
        return targetStart + (targetStop - targetStart) * ((value - start) / (stop - start));
    }

    protected static void validateString(String s) throws IllegalStateException{
        if (s.isBlank()) throw new IllegalStateException("Field is empty");
        if (s.length() < 8) throw new IllegalStateException("Field is too short. The minimum length is 8 characters");
        if (s.length() > 64) throw new IllegalStateException("Field is too long. The maximum length is 64 characters");
        if (!s.matches("\\w+")) throw new IllegalStateException("Field contains illegal characters. Only latin upper-case and lower-case, numbers are allowed");
    }


}
