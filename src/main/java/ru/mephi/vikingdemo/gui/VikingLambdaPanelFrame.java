package ru.mephi.vikingdemo.gui;

import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.StoredViking;
import ru.mephi.vikingdemo.service.VikingLambdaService;
import ru.mephi.vikingdemo.service.VikingService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VikingLambdaPanelFrame extends JFrame {

    private final VikingLambdaService lambdaService;
    private final VikingService vikingService;
    private final Runnable refreshAction;
    private final JTextArea outputArea = new JTextArea();

    public VikingLambdaPanelFrame(
            VikingLambdaService lambdaService,
            VikingService vikingService,
            Runnable refreshAction
    ) {
        this.lambdaService = lambdaService;
        this.vikingService = vikingService;
        this.refreshAction = refreshAction;

        setTitle("Lambda tools");
        setSize(new Dimension(820, 460));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JLabel header = new JLabel("Lambda tools", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        add(header, BorderLayout.NORTH);

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 1, 4, 4));
        actions.add(button("Age count", this::showAgeCounters));
        actions.add(button("Beard + hair", this::showAppearanceCounter));
        actions.add(button("Axe count", this::showAxeCounters));
        actions.add(button("Tall random", this::showTallRandom));
        actions.add(button("Legendary", this::showLegendary));
        actions.add(button("Red by age", this::showRedSorted));
        actions.add(button("IDs", this::showIdOperations));
        actions.add(button("Generate batch", this::showBatchGeneration));
        add(actions, BorderLayout.EAST);
    }

    private JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void showAgeCounters() {
        String[] modes = {"Older than", "Younger than", "In range", "Outside range"};
        String mode = (String) JOptionPane.showInputDialog(
                this,
                "Age condition:",
                "Age count",
                JOptionPane.QUESTION_MESSAGE,
                null,
                modes,
                modes[0]
        );
        if (mode == null) {
            return;
        }

        Long count = switch (mode) {
            case "Older than" -> {
                Integer age = inputInteger("Age:", 30);
                yield age == null ? null : lambdaService.countOlderThan(age);
            }
            case "Younger than" -> {
                Integer age = inputInteger("Age:", 30);
                yield age == null ? null : lambdaService.countYoungerThan(age);
            }
            case "In range" -> {
                int[] range = inputAgeRange();
                yield range == null ? null : lambdaService.countAgeBetween(range[0], range[1]);
            }
            case "Outside range" -> {
                int[] range = inputAgeRange();
                yield range == null ? null : lambdaService.countAgeOutside(range[0], range[1]);
            }
            default -> null;
        };

        if (count != null) {
            outputArea.setText(mode + System.lineSeparator() + "Count: " + count);
        }
    }

    private void showAppearanceCounter() {
        BeardStyle beardStyle = (BeardStyle) JOptionPane.showInputDialog(
                this,
                "Beard style:",
                "Beard + hair",
                JOptionPane.QUESTION_MESSAGE,
                null,
                BeardStyle.values(),
                BeardStyle.SHORT
        );
        if (beardStyle == null) {
            return;
        }

        HairColor hairColor = (HairColor) JOptionPane.showInputDialog(
                this,
                "Hair color:",
                "Beard + hair",
                JOptionPane.QUESTION_MESSAGE,
                null,
                HairColor.values(),
                HairColor.Red
        );
        if (hairColor == null) {
            return;
        }

        long count = lambdaService.countByBeardAndHair(beardStyle, hairColor);
        outputArea.setText("Beard: " + beardStyle + System.lineSeparator()
                + "Hair: " + hairColor + System.lineSeparator()
                + "Count: " + count);
    }

    private void showAxeCounters() {
        outputArea.setText("Vikings with one axe: " + lambdaService.countWithOneAxe()
                + System.lineSeparator()
                + "Vikings with two axes: " + lambdaService.countWithTwoAxes());
    }

    private void showTallRandom() {
        Optional<StoredViking> viking = lambdaService.randomTallerThan180();
        outputArea.setText(viking.map(this::formatViking).orElse("No vikings taller than 180 cm"));
    }

    private void showLegendary() {
        outputArea.setText(formatVikings(lambdaService.findWithLegendaryEquipment()));
    }

    private void showRedSorted() {
        outputArea.setText(formatVikings(lambdaService.findRedHairedSortedByAge()));
    }

    private void showIdOperations() {
        Integer[] ids = lambdaService.currentIds();
        String maxId = lambdaService.maxId(ids)
                .map(String::valueOf)
                .orElse("none");

        outputArea.setText("Max ID: " + maxId + System.lineSeparator()
                + "Even IDs: " + formatIds(lambdaService.evenIds(ids).toArray(Integer[]::new)));
    }

    private void showBatchGeneration() {
        Integer count = inputInteger("How many vikings:", 10);
        if (count == null) {
            return;
        }
        if (count <= 0) {
            showMessage("Count must be positive");
            return;
        }

        List<StoredViking> created = vikingService.generateRandomVikings(count);
        refreshAction.run();
        outputArea.setText("Created vikings: " + created.size());
    }

    private int[] inputAgeRange() {
        Integer min = inputInteger("Min age:", 25);
        if (min == null) {
            return null;
        }

        Integer max = inputInteger("Max age:", 45);
        if (max == null) {
            return null;
        }

        if (min > max) {
            showMessage("Min age must be less than or equal to max age");
            return null;
        }

        return new int[]{min, max};
    }

    private Integer inputInteger(String message, int initialValue) {
        String value = JOptionPane.showInputDialog(this, message, initialValue);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            showMessage("Value must be an integer");
            return null;
        }
    }

    private String formatIds(Integer[] ids) {
        return Arrays.stream(ids)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private String formatVikings(List<StoredViking> vikings) {
        if (vikings.isEmpty()) {
            return "No vikings found";
        }

        return vikings.stream()
                .map(this::formatViking)
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
    }

    private String formatViking(StoredViking viking) {
        return "ID: " + viking.id() + System.lineSeparator()
                + "Name: " + viking.name() + System.lineSeparator()
                + "Age: " + viking.age() + System.lineSeparator()
                + "Height: " + viking.heightCm() + " cm" + System.lineSeparator()
                + "Hair: " + viking.hairColor() + System.lineSeparator()
                + "Beard: " + viking.beardStyle() + System.lineSeparator()
                + "Equipment: " + formatEquipment(viking.equipment());
    }

    private String formatEquipment(List<EquipmentItem> equipment) {
        if (equipment == null) {
            return "";
        }

        return equipment.stream()
                .map(item -> item.name() + " [" + item.quality() + "]")
                .collect(Collectors.joining(", "));
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
