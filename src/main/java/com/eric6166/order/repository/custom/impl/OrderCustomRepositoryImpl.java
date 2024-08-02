package com.eric6166.order.repository.custom.impl;

import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.repository.custom.OrderCustomRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Page<OrderDto> findAllOrderByUsername(String username, int days, Integer pageNumber, Integer pageSize) {
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        var orderDate = LocalDateTime.now().minusDays(days);
        var sqlCount = """
                SELECT COUNT(*)
                FROM (
                         SELECT O1.ORDER_DATE, MAX(O1.ORDER_STATUS_VALUE) AS MAX_STATUS
                    FROM T_ORDER O1
                    WHERE O1.ORDER_DATE > :orderDate
                      AND O1.USERNAME = :username
                    GROUP BY O1.ORDER_DATE
                     ) O2
                """;
        var paramSourceCount = new MapSqlParameterSource();
        paramSourceCount.addValue("username", username);
        paramSourceCount.addValue("orderDate", orderDate);
        var total = ObjectUtils.defaultIfNull(namedParameterJdbcTemplate.queryForObject(sqlCount, paramSourceCount, Long.class), NumberUtils.LONG_ZERO);

        var sql = """
                WITH LATEST_ORDER_STATUS AS (
                    SELECT O1.ORDER_DATE, MAX(O1.ORDER_STATUS_VALUE) AS MAX_STATUS
                    FROM T_ORDER O1
                    WHERE O1.ORDER_DATE > :orderDate
                      AND O1.USERNAME = :username
                    GROUP BY O1.ORDER_DATE
                    ORDER BY O1.ORDER_DATE DESC
                    OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
                )
                SELECT O.*
                FROM T_ORDER O
                         JOIN LATEST_ORDER_STATUS L
                              ON O.ORDER_DATE = L.ORDER_DATE AND O.USERNAME = :username
                                  AND O.ORDER_STATUS_VALUE = L.MAX_STATUS
                WHERE O.ORDER_DATE > :orderDate
                ORDER BY O.ORDER_DATE DESC;
                """;
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("username", username);
        paramSource.addValue("offset", pageable.getOffset());
        paramSource.addValue("pageSize", pageable.getPageSize());
        paramSource.addValue("orderDate", orderDate);
        var orderList = namedParameterJdbcTemplate.query(sql, paramSource, BeanPropertyRowMapper.newInstance(OrderDto.class));
        return new PageImpl<>(orderList, pageable, total);
    }
}
