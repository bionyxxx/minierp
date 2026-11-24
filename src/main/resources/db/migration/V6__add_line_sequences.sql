-- Line ids are sequence-based (like their parent documents) so that a document
-- and its lines are inserted together in a single flush.
CREATE SEQUENCE purchase_order_line_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE goods_receipt_line_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE sales_order_line_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE delivery_line_seq START WITH 1 INCREMENT BY 1;
