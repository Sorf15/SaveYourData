package com.sorf.syd.util;

import com.sorf.syd.Main;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Config {
    public static ArrayList<Pair<String, Object>> keys = new ArrayList<>();
    private static final File config = new File(Main.directory + "/conf.properties");

    private static final @NotNull FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure((new Parameters()).properties()
                    .setFileName(config.getName())
                    .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

    public static synchronized void read() {
        if(!config.exists()) {
            try {
                config.createNewFile();
                createDefault();
            } catch (ConfigurationException | IOException e) {
                e.printStackTrace();
            }
        }
        keys.clear();
        try
        {
            Configuration config = builder.getConfiguration();
            keys.add(new Pair<>("version", config.getString("version", Main.version)));
            keys.add(new Pair<>("logged_in", config.getBoolean("logged_in", false)));
            keys.add(new Pair<>("keep_signed_in", config.getBoolean("keep_signed_in", false)));
            keys.add(new Pair<>("working", config.getBoolean("working", false)));
            keys.add(new Pair<>("genLength", config.getInt("genLength", 16)));
            keys.add(new Pair<>("genLower", config.getBoolean("genLower", true)));
            keys.add(new Pair<>("genUpper", config.getBoolean("genUpper", false)));
            keys.add(new Pair<>("genNumbers", config.getBoolean("genNumbers", false)));
            builder.save();

        }
        catch(ConfigurationException | ConversionException e)
        {
            Logger.error("Caught an error during config read operation!");
            e.printStackTrace();
        }
    }

    public static synchronized Object getValue(@NotNull String key) throws ConfigurationException {
        for (Pair<String, Object> pair : keys) {
            if (pair.getKey().equals(key)) {
                return pair.getValue();
            }
        }
        throw new ConfigurationException();
    }

    public static synchronized boolean write(@NotNull String property, @NotNull Object value) throws ConfigurationException {
        if (property.isBlank()) {
            return false;
        }
        Configuration config = builder.getConfiguration();
        config.setProperty(property, value);
        builder.save();
        keys.removeIf(p -> p.getKey().equals(property));
        keys.add(new Pair<>(property, value));
        return true;
    }


    public static synchronized void createDefault() throws ConfigurationException {
        Configuration config = builder.getConfiguration();
        config.setProperty("version", Main.version);
        config.setProperty("logged_in", false);
        config.setProperty("keep_signed_in", false);
        config.setProperty("working", false);
        config.setProperty("genLength", 16);
        config.setProperty("genLower", true);
        config.setProperty("genUpper", false);
        config.setProperty("genNumbers", false);
        builder.save();
    }
}
