package ru.mephi.vikingdemo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Сохраненный викинг с идентификатором из БД")
public record StoredViking(
        @Schema(description = "Идентификатор викинга в БД", example = "1")
        Integer id,
        @Schema(description = "Параметры викинга")
        Viking viking
) {
    public String name() {
        return viking.name();
    }

    public int age() {
        return viking.age();
    }

    public int heightCm() {
        return viking.heightCm();
    }

    public HairColor hairColor() {
        return viking.hairColor();
    }

    public BeardStyle beardStyle() {
        return viking.beardStyle();
    }

    public List<EquipmentItem> equipment() {
        return viking.equipment();
    }
}
