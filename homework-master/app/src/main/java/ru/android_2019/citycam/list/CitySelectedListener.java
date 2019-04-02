package ru.android_2019.citycam.list;

import ru.android_2019.citycam.model.City;

/**
 * Интерфейс дял получения событий от списка городов.
 */
public interface CitySelectedListener {
    /**
     * Вызывается, когда указанный город был выбран в списке городов.
     */
    void onCitySelected(City city);
}
