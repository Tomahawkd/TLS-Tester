package io.tomahawkd.database;

public @interface StatisticMapping {

	String column() default "";

	int position() default 0;
}
