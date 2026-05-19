package ru.mephi.vikingdemo.service;

import org.springframework.stereotype.Service;
import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.StoredViking;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

@Service
public class VikingLambdaService {

    private final VikingService vikingService;
    private final Random random = new Random();

    public VikingLambdaService(VikingService vikingService) {
        this.vikingService = vikingService;
    }

    public long countOlderThan(int age) {
        return countBy(viking -> viking.age() > age);
    }

    public long countYoungerThan(int age) {
        return countBy(viking -> viking.age() < age);
    }

    public long countAgeBetween(int minAge, int maxAge) {
        return countBy(viking -> viking.age() >= minAge && viking.age() <= maxAge);
    }

    public long countAgeOutside(int minAge, int maxAge) {
        return countBy(viking -> viking.age() < minAge || viking.age() > maxAge);
    }

    public long countByBeardAndHair(BeardStyle beardStyle, HairColor hairColor) {
        return countBy(viking -> viking.beardStyle() == beardStyle && viking.hairColor() == hairColor);
    }

    public long countWithOneAxe() {
        return countBy(viking -> countAxes(viking.equipment()) == 1);
    }

    public long countWithTwoAxes() {
        return countBy(viking -> countAxes(viking.equipment()) == 2);
    }

    public Optional<StoredViking> randomTallerThan180() {
        List<StoredViking> matches = vikingService.findAll().stream()
                .filter(viking -> viking.heightCm() > 180)
                .toList();

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(matches.get(random.nextInt(matches.size())));
    }

    public List<StoredViking> findWithLegendaryEquipment() {
        return vikingService.findAll().stream()
                .filter(viking -> equipmentOf(viking).stream().anyMatch(this::isLegendary))
                .toList();
    }

    public List<StoredViking> findRedHairedSortedByAge() {
        return vikingService.findAll().stream()
                .filter(viking -> viking.hairColor() == HairColor.Red)
                .sorted(Comparator.comparingInt(StoredViking::age))
                .toList();
    }

    public Optional<Integer> maxId(Integer[] ids) {
        return Arrays.stream(ids)
                .filter(Objects::nonNull)
                .max(Integer::compareTo);
    }

    public List<Integer> evenIds(Integer[] ids) {
        return Arrays.stream(ids)
                .filter(Objects::nonNull)
                .filter(id -> id % 2 == 0)
                .toList();
    }

    public Integer[] currentIds() {
        return vikingService.findAll().stream()
                .map(StoredViking::id)
                .filter(Objects::nonNull)
                .toArray(Integer[]::new);
    }

    private long countBy(Predicate<StoredViking> condition) {
        return vikingService.findAll().stream()
                .filter(condition)
                .count();
    }

    private long countAxes(List<EquipmentItem> equipment) {
        if (equipment == null) {
            return 0;
        }

        return equipment.stream()
                .filter(item -> item.name() != null)
                .filter(item -> item.name().equalsIgnoreCase("Axe"))
                .count();
    }

    private List<EquipmentItem> equipmentOf(StoredViking viking) {
        if (viking.equipment() == null) {
            return List.of();
        }

        return viking.equipment();
    }

    private boolean isLegendary(EquipmentItem item) {
        return item.quality() != null && item.quality().equalsIgnoreCase("Legendary");
    }
}
