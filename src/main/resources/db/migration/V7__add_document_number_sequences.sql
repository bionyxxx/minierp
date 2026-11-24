-- Document numbers are allocated before the entity is persisted (Hibernate
-- snapshots entity state at persist time), so each document type owns a number
-- sequence that is independent from its surrogate id sequence.
CREATE SEQUENCE purchase_order_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE goods_receipt_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE sales_order_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE delivery_number_seq START WITH 1 INCREMENT BY 1;
