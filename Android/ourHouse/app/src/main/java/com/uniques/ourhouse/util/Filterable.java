package com.uniques.ourhouse.util;

import java.util.Date;
import java.util.List;

public interface Filterable extends Model {

    List<Nameable> filter(Date date);

    List<Nameable> filterRecursively(Date date);
}
