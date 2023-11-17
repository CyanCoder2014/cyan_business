package com.cyancoder.taxpaysys.modules.tax_api.repository;

import com.cyancoder.taxpaysys.modules.tax_api.entity.factor.Factor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FactorRepository extends JpaRepository<Factor, Integer> {


    List<Factor> findByCreatedOnBetween(Date startOn, Date endOn);


    List<Factor> findByCodeBetween(String codeFrom, String codeTo);

    List<Factor> findByCode(String code);

    List<Factor> findById(Long id);


    String query =

            "SELECT " +
                    "  b.id, " +
                    "  b.sarjam, " +
                    "  b.azTarigheHagholAmalkari, " +
                    "  b.product_type_code, " +
                    "  b.product_type_name, " +
                    "  b.product_description, " +
                    "  b.product_code, " +
                    "  b.bargashti, " +
                    "  b.product_amount, " +
                    "  b.added_value_amount, " +
                    "  b.tax_amount, " +
                    "  b.other_tax_amount, " +
                    "  b.discount_amount, " +
                    "  b.person, " +
                    "  b.post_code, " +
                    "  b.dial_code, " +
                    "  b.tell_number, " +
                    "  b.address, " +
                    "  b.full_name, " +
                    "  b.company_name, " +
                    "  b.economic_code, " +
                    "  b.national_code, " +
                    "  b.seller, " +
                    "  b.buyer_type_code, " +
                    "  b.buyer_type_name, " +
                    "  b.state_code, " +
                    "  b.state_name, " +
                    "  b.city_code, " +
                    "  b.city_name, " +
                    "  c.code AS currency_code, " +
                    "  c.name AS currency_name, " +
                    "  b.product_currency_amount, " +
                    "  b.product_exchange_rate, " +
                    "  b.product_rial_amount, " +
                    "  b.added_value_currency_amount, " +
                    "  b.added_value_exchange_rate, " +
                    "  b.added_value_rial_amount, " +
                    "  b.tax_currency_amount, " +
                    "  b.tax_exchange_rate, " +
                    "  b.tax_rial_amount, " +
                    "  b.other_tax_currency_amount, " +
                    "  b.other_tax_exchange_rate, " +
                    "  b.other_tax_rial_amount, " +
                    "  b.discount_currency_amount, " +
                    "  b.discount_exchange_rate, " +
                    "  b.discount_rial_amount, " +
                    "  b.bill_code, " +
                    "  b.bill_date, " +
                    "  b.accounting_document_code, " +
                    "  b.accounting_document_date, " +
                    "  b.created_on, " +
                    "  b.is_sent, " +
                    "  b.status, " +
                    "  b.type " +
                    "FROM (SELECT " +
                    "        b.id, " +
                    "        b.sarjam, " +
                    "        b.azTarigheHagholAmalkari, " +
                    "        b.product_type_code, " +
                    "        b.product_type_name, " +
                    "        b.product_description, " +
                    "        b.product_code, " +
                    "        b.bargashti, " +
                    "        b.product_amount, " +
                    "        b.added_value_amount, " +
                    "        b.tax_amount, " +
                    "        b.other_tax_amount, " +
                    "        b.discount_amount, " +
                    "        b.person, " +
                    "        b.post_code, " +
                    "        c.dial_code, " +
                    "        b.tell_number, " +
                    "        b.address, " +
                    "        b.full_name, " +
                    "        b.company_name, " +
                    "        b.economic_code, " +
                    "        b.national_code, " +
                    "        b.seller, " +
                    "        b.buyer_type_code, " +
                    "        b.buyer_type_name, " +
                    "        c.state_code, " +
                    "        c.state_name, " +
                    "        c.city_code, " +
                    "        c.city_name, " +
                    "        b.currency, " +
                    "        b.product_currency_amount, " +
                    "        b.product_exchange_rate, " +
                    "        b.product_rial_amount, " +
                    "        b.added_value_currency_amount, " +
                    "        b.added_value_exchange_rate, " +
                    "        b.added_value_rial_amount, " +
                    "        b.tax_currency_amount, " +
                    "        b.tax_exchange_rate, " +
                    "        b.tax_rial_amount, " +
                    "        b.other_tax_currency_amount, " +
                    "        b.other_tax_exchange_rate, " +
                    "        b.other_tax_rial_amount, " +
                    "        b.discount_currency_amount, " +
                    "        b.discount_exchange_rate, " +
                    "        b.discount_rial_amount, " +
                    "        b.bill_code, " +
                    "        b.bill_date, " +
                    "        b.accounting_document_code, " +
                    "        b.accounting_document_date, " +
                    "        b.created_on, " +
                    "        b.is_sent, " +
                    "        b.status, " +
                    "        b.type " +
                    "      FROM (SELECT " +
                    "              b.id, " +
                    "              b.sarjam, " +
                    "              b.azTarigheHagholAmalkari, " +
                    "              b.product_type_code, " +
                    "              b.product_type_name, " +
                    "              b.product_description, " +
                    "              b.product_code, " +
                    "              b.bargashti, " +
                    "              b.product_amount, " +
                    "              b.added_value_amount, " +
                    "              b.tax_amount, " +
                    "              b.other_tax_amount, " +
                    "              b.discount_amount, " +
                    "              b.person, " +
                    "              b.post_code, " +
                    "              b.tell_number, " +
                    "              b.address, " +
                    "              b.full_name, " +
                    "              b.company_name, " +
                    "              b.economic_code, " +
                    "              b.national_code, " +
                    "              b.seller, " +
                    "              bt.code AS buyer_type_code, " +
                    "              bt.name AS buyer_type_name, " +
                    "              b.city, " +
                    "              b.currency, " +
                    "              b.product_currency_amount, " +
                    "              b.product_exchange_rate, " +
                    "              b.product_rial_amount, " +
                    "              b.added_value_currency_amount, " +
                    "              b.added_value_exchange_rate, " +
                    "              b.added_value_rial_amount, " +
                    "              b.tax_currency_amount, " +
                    "              b.tax_exchange_rate, " +
                    "              b.tax_rial_amount, " +
                    "              b.other_tax_currency_amount, " +
                    "              b.other_tax_exchange_rate, " +
                    "              b.other_tax_rial_amount, " +
                    "              b.discount_currency_amount, " +
                    "              b.discount_exchange_rate, " +
                    "              b.discount_rial_amount, " +
                    "              b.bill_code, " +
                    "              b.bill_date, " +
                    "              b.accounting_document_code, " +
                    "              b.accounting_document_date, " +
                    "              b.created_on, " +
                    "              b.is_sent, " +
                    "              b.status, " +
                    "              b.type " +
                    "            FROM (SELECT " +
                    "                    b.id, " +
                    "                    b.sarjam, " +
                    "                    b.azTarigheHagholAmalkari, " +
                    "                    p.code AS product_type_code, " +
                    "                    p.name AS product_type_name, " +
                    "                    b.product_code, " +
                    "                    b.product_description, " +
                    "                    b.bargashti, " +
                    "                    b.product_amount, " +
                    "                    b.added_value_amount, " +
                    "                    b.tax_amount, " +
                    "                    b.other_tax_amount, " +
                    "                    b.discount_amount, " +
                    "                    b.person, " +
                    "                    b.post_code, " +
                    "                    b.tell_number, " +
                    "                    b.address, " +
                    "                    b.full_name, " +
                    "                    b.company_name, " +
                    "                    b.economic_code, " +
                    "                    b.national_code, " +
                    "                    b.seller_type, " +
                    "                    b.seller, " +
                    "                    b.city, " +
                    "                    b.currency, " +
                    "                    b.product_currency_amount, " +
                    "                    b.product_exchange_rate, " +
                    "                    b.product_rial_amount, " +
                    "                    b.added_value_currency_amount, " +
                    "                    b.added_value_exchange_rate, " +
                    "                    b.added_value_rial_amount, " +
                    "                    b.tax_currency_amount, " +
                    "                    b.tax_exchange_rate, " +
                    "                    b.tax_rial_amount, " +
                    "                    b.other_tax_currency_amount, " +
                    "                    b.other_tax_exchange_rate, " +
                    "                    b.other_tax_rial_amount, " +
                    "                    b.discount_currency_amount, " +
                    "                    b.discount_exchange_rate, " +
                    "                    b.discount_rial_amount, " +
                    "                    b.bill_code, " +
                    "                    b.bill_date, " +
                    "                    b.accounting_document_code, " +
                    "                    b.accounting_document_date, " +
                    "                    b.created_on, " +
                    "                    b.is_sent, " +
                    "                    b.status, " +
                    "                    b.type " +
                    "                  FROM (SELECT " +
                    "                          b.buy_detail_id AS id, " +
                    "                          b.sarjam, " +
                    "                          b.azTarigheHagholAmalkari, " +
                    "                          b.product_type, " +
                    "                          b.product_description, " +
                    "                          b.product_code, " +
                    "                          b.bargashti, " +
                    "                          b.product_amount, " +
                    "                          b.added_value_amount, " +
                    "                          b.tax_amount, " +
                    "                          b.other_tax_amount, " +
                    "                          b.discount_amount, " +
                    "                          b.person, " +
                    "                          b.post_code, " +
                    "                          b.tell_number, " +
                    "                          b.address, " +
                    "                          b.full_name, " +
                    "                          b.company_name, " +
                    "                          b.economic_code, " +
                    "                          b.national_code, " +
                    "                          b.seller_type, " +
                    "                          b.seller, " +
                    "                          b.city, " +
                    "                          b.currency, " +
                    "                          b.product_currency_amount, " +
                    "                          b.product_exchange_rate, " +
                    "                          b.product_rial_amount, " +
                    "                          b.added_value_currency_amount, " +
                    "                          b.added_value_exchange_rate, " +
                    "                          b.added_value_rial_amount, " +
                    "                          b.tax_currency_amount, " +
                    "                          b.tax_exchange_rate, " +
                    "                          b.tax_rial_amount, " +
                    "                          b.other_tax_currency_amount, " +
                    "                          b.other_tax_exchange_rate, " +
                    "                          b.other_tax_rial_amount, " +
                    "                          b.discount_currency_amount, " +
                    "                          b.discount_exchange_rate, " +
                    "                          b.discount_rial_amount, " +
                    "                          b.bill_code, " +
                    "                          b.bill_date, " +
                    "                          b.accounting_document_code, " +
                    "                          b.accounting_document_date, " +
                    "                          b.created_on, " +
                    "                          b.is_sent, " +
                    "                          b.status, " +
                    "                          1               AS type " +
                    "                        FROM c_buy_detail b " +
                    "                        UNION ALL SELECT " +
                    "                                    f.sub_factor_id                      AS id, " +
                    "                                    null                                 AS sarjam, " +
                    "                                    null                                 AS azTarigheHagholAmalkari, " +
                    "                                    f.product_type, " +
//                "                                    CONCAT(COALESCE(f.product_name, ''), " +
//                "                                           IF((f.product_name IS NOT NULL && f.distribution IS NOT NULL), ' - ', ''), " +
//                "                                           COALESCE(f.distribution, '')) AS product_description, " +
                    "                                    f.product_name                       AS product_description, " +
                    "                                    null                                 AS product_code, " +
                    "                                    null                                 AS bargashti, " +
                    "                                    f.final_price                        AS product_amount, " +
                    "                                    f.tax                                AS added_value_amount, " +
                    "                                    null                                 AS tax_amount, " +
                    "                                    null                                 AS other_tax_amount, " +
                    "                                    f.discount                           AS discount_amount, " +
                    "                                    f.person                             AS person, " +
                    "                                    f.post_code, " +
                    "                                    f.tell_number, " +
                    "                                    f.address, " +
                    "                                    f.full_name, " +
                    "                                    f.full_name                          AS company_name, " +
                    "                                    f.economic_code, " +
                    "                                    f.national_code, " +
                    "                                    f.buyer_type                         AS seller_type, " +
                    "                                    f.seller                             AS seller, " +
                    "                                    f.city, " +
                    "                                    null                                 AS currency, " +
                    "                                    null                                 AS product_currency_amount, " +
                    "                                    null                                 AS product_exchange_rate, " +
                    "                                    null                                 AS product_rial_amount, " +
                    "                                    null                                 AS added_value_currency_amount, " +
                    "                                    null                                 AS added_value_exchange_rate, " +
                    "                                    null                                 AS added_value_rial_amount, " +
                    "                                    null                                 AS tax_currency_amount, " +
                    "                                    null                                 AS tax_exchange_rate, " +
                    "                                    null                                 AS tax_rial_amount, " +
                    "                                    null                                 AS other_tax_currency_amount, " +
                    "                                    null                                 AS other_tax_exchange_rate, " +
                    "                                    null                                 AS other_tax_rial_amount, " +
                    "                                    null                                 AS discount_currency_amount, " +
                    "                                    null                                 AS discount_exchange_rate, " +
                    "                                    null                                 AS discount_rial_amount, " +
                    "                                    f.code                               AS bill_code, " +
                    "                                    f.factor_date                        AS bill_date, " +
                    "                                    null                                 AS accounting_document_code, " +
                    "                                    f.factor_date                        AS accounting_document_date, " +
                    "                                    f.factor_date                        AS created_on, " +
                    "                                    null                                 AS is_sent, " +
                    "                                    f.status, " +
                    "                                    0                                    AS type " +
                    "                                  FROM c_sub_factor f) b LEFT JOIN c_product_type p " +
                    "                      ON b.product_type = p.product_type_id) b LEFT JOIN c_buyer_type bt " +
                    "                ON b.seller_type = bt.buyer_type_id) b LEFT JOIN (SELECT " +
                    "                                                                    c.city_id, " +
                    "                                                                    s.code AS state_code, " +
                    "                                                                    s.name AS state_name, " +
                    "                                                                    c.code AS city_code, " +
                    "                                                                    c.name AS city_name, " +
                    "                                                                    c.dial_code " +
                    "                                                                  FROM c_city c LEFT JOIN c_state s " +
                    "                                                                      on c.ste = s.state_id) c " +
                    "          ON b.city = c.city_id) b LEFT JOIN c_currency c ON b.currency = c.currency_id " +
                    "WHERE (b.status <> 'removed' OR b.status IS NULL)  ";




//    @Query(query)
//    public List<Object> getFactorsByDate();

















}
