package com.eric6166.order.repository.custom.impl;

import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.model.Order;
import com.eric6166.order.repository.custom.OrderCustomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Page<Order> findAllOrderByUsername(String username, Integer pageNumber, Integer pageSize) {
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        String sql = """
                WITH LATESTORDERSTATUS AS (
                    SELECT O1.ORDER_DATE, MAX(O1.ORDER_STATUS_VALUE) AS MAX_STATUS
                    FROM T_ORDER O1
                    WHERE O1.USERNAME = :username
                    GROUP BY O1.ORDER_DATE
                    ORDER BY O1.ORDER_DATE DESC
                    OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
                )
                SELECT O.*
                FROM T_ORDER O
                JOIN LATESTORDERSTATUS L
                    ON O.USERNAME = :username
                    AND O.ORDER_DATE = L.ORDER_DATE
                    AND O.ORDER_STATUS_VALUE = L.MAX_STATUS
                ORDER BY O.ORDER_DATE DESC
                """;
        var mapSql = new MapSqlParameterSource();
        mapSql.addValue("username", username);
        mapSql.addValue("offset", pageable.getOffset());
        mapSql.addValue("pageSize", pageable.getPageSize());
        var orderList = namedParameterJdbcTemplate.query(sql, mapSql, BeanPropertyRowMapper.newInstance(Order.class));

        String sqlCount = """
                SELECT COUNT(*)
                FROM (
                         SELECT 1
                         FROM T_ORDER O1
                         WHERE O1.USERNAME = :username
                         GROUP BY O1.ORDER_DATE
                     ) O2;      
                """;
        var mapSqlCount = new MapSqlParameterSource();
        mapSqlCount.addValue("username", username);
        var total = namedParameterJdbcTemplate.queryForObject(sqlCount, mapSqlCount, Long.class);

        return new PageImpl<>(orderList, pageable, total);
    }
}
