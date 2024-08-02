BEGIN;

CREATE TABLE IF NOT EXISTS T_ORDER
(
    ORDER_DATE         TIMESTAMP(6) NOT NULL,
    USERNAME           VARCHAR(255) NOT NULL,
    ORDER_STATUS_VALUE REAL         NOT NULL,
    ORDER_DETAIL       TEXT,
    TOTAL_AMOUNT       NUMERIC(19, 4),
    CREATED_BY         VARCHAR(255),
    CREATED_DATE       TIMESTAMP(6),
    LAST_MODIFIED_BY   VARCHAR(255),
    LAST_MODIFIED_DATE TIMESTAMP(6),
    PRIMARY KEY (ORDER_DATE, USERNAME, ORDER_STATUS_VALUE)
) PARTITION BY RANGE (ORDER_DATE);

DO
$$
    DECLARE
        INITIAL_DATE DATE := CURRENT_DATE - INTERVAL '1 years';
        START_DATE   DATE := INITIAL_DATE;
        END_DATE     DATE;
    BEGIN
        -- Create an initial partition for dates less than the initial_date
        EXECUTE FORMAT('CREATE TABLE IF NOT EXISTS T_ORDER_P0 PARTITION OF T_ORDER FOR VALUES FROM (MINVALUE) TO (%L)',
                       INITIAL_DATE);

        -- Create monthly partitions for dates starting from initial_date up to one month beyond the current date
        WHILE START_DATE <= (CURRENT_DATE + INTERVAL '1 month')
            LOOP
                END_DATE := START_DATE + INTERVAL '1 month';
                EXECUTE FORMAT(
                        'CREATE TABLE IF NOT EXISTS T_ORDER_%s PARTITION OF T_ORDER FOR VALUES FROM (%L) TO (%L)',
                        TO_CHAR(START_DATE, 'YYYY_MM'), START_DATE, END_DATE);
                START_DATE := END_DATE;
            END LOOP;
    END
$$;


COMMIT;