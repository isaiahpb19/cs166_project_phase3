DROP SEQUENCE IF EXISTS order_seq;
CREATE SEQUENCE order_seq START WITH 5000
increment by 1;

CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION order_inc()
RETURNS "trigger" AS
$BODY$
BEGIN
    NEW.rentalOrderID := 'gamerentalorder'||nextval('order_seq')::varchar(50);
    NEW.orderTimestamp := current_date;
    NEW.dueDate := current_date + interval '1 month';
    RETURN NEW;
END
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS inc_order ON RentalOrder;
CREATE TRIGGER inc_order BEFORE INSERT
ON RentalOrder FOR EACH ROW
EXECUTE PROCEDURE order_inc();


DROP SEQUENCE IF EXISTS track_seq;
CREATE SEQUENCE track_seq START WITH 5000
increment by 1;

CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION track_inc()
RETURNS "trigger" AS
$BODY$
BEGIN
    NEW.trackingID := 'trackingid' ||nextval('track_seq')::varchar(50);
    NEW.lastUpdateDate := current_date;
    RETURN NEW;
END
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS inc_track ON TrackingInfo;
CREATE TRIGGER inc_track BEFORE INSERT
ON TrackingInfo FOR EACH ROW
EXECUTE PROCEDURE track_inc();