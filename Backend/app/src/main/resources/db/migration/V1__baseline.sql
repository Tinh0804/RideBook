--
-- PostgreSQL database dump
--


-- Dumped from database version 15.18 (Debian 15.18-1.pgdg13+1)
-- Dumped by pg_dump version 15.18 (Debian 15.18-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: bookingstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.bookingstatus AS ENUM (
    'ACCEPTED',
    'ARRIVED',
    'CANCELLED',
    'COMPLETED',
    'IN_PROGRESS',
    'PENDING'
);


--
-- Name: customerpromotionstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.customerpromotionstatus AS ENUM (
    'EXPIRED',
    'SAVED',
    'USED'
);


--
-- Name: discounttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.discounttype AS ENUM (
    'FIXED_AMOUNT',
    'PERCENTAGE'
);


--
-- Name: paymentmethod; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.paymentmethod AS ENUM (
    'CASH',
    'MOMO',
    'ONLINE',
    'VNPAY'
);


--
-- Name: predefinedrole; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.predefinedrole AS ENUM (
    'ADMIN',
    'CUSTOMER',
    'DRIVER'
);


--
-- Name: provider; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.provider AS ENUM (
    'FACEBOOK',
    'GOOGLE',
    'LOCAL'
);


--
-- Name: rejectiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.rejectiontype AS ENUM (
    'IGNORED',
    'REJECTED'
);


--
-- Name: transactionstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.transactionstatus AS ENUM (
    'CANCELLED',
    'COMPLETED',
    'FAILED',
    'PENDING'
);


--
-- Name: transactiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.transactiontype AS ENUM (
    'DEPOSIT',
    'PAYMENT',
    'TRIP_FEE',
    'TRIP_INCOME',
    'WITHDRAWAL'
);


--
-- Name: walletstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.walletstatus AS ENUM (
    'ACTIVE',
    'BLOCKED',
    'INACTIVE'
);


--
-- Name: CAST (public.bookingstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.bookingstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.customerpromotionstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.customerpromotionstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.discounttype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.discounttype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.paymentmethod AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.paymentmethod AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.predefinedrole AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.predefinedrole AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.provider AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.provider AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.rejectiontype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.rejectiontype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.transactionstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.transactionstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.transactiontype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.transactiontype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.bookingstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.bookingstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.customerpromotionstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.customerpromotionstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.discounttype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.discounttype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.paymentmethod); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.paymentmethod) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.predefinedrole); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.predefinedrole) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.provider); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.provider) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.rejectiontype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.rejectiontype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.transactionstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.transactionstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.transactiontype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.transactiontype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.walletstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.walletstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.walletstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.walletstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: pr_findavailabledriversclosercustomer(double precision, double precision, double precision, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.pr_findavailabledriversclosercustomer(lat double precision, lng double precision, radius double precision, p_vehicle_type_id character varying) RETURNS TABLE(driver_id character varying, account_id character varying, vehicle_type_id character varying, phone character varying, email character varying, citizen_id character varying, license_plate character varying, driver_name character varying, birth_date date, driving_license character varying, vehicle_name character varying, current_lat double precision, current_lng double precision, score double precision, last_trip_time timestamp without time zone, address character varying, area character varying, avatar character varying, criminal_record character varying, gender character varying, activity_status boolean, distance double precision)
    LANGUAGE plpgsql
    AS $$
begin
	return query
	select d.driver_id, d.account_id, d.vehicle_type_id, d.phone, d.email, d.citizen_id, d.license_plate, d.driver_name, d.birth_date, d.driving_license, d.vehicle_name, d.current_lat, d.current_lng, d.score, d.last_trip_time, d.address, d.area, d.avatar, d.criminal_record, d.gender, d.activity_status, (6371 * acos(
		            cos(radians(lat)) * cos(radians(d.current_lat))
		            * cos(radians(d.current_lng) - radians(lng))
		            + sin(radians(lat)) * sin(radians(d.current_lat))
		        )) AS Distance
	from driver d
	join vehicle_type vt on vt.vehicle_type_id = d.vehicle_type_id
	where d.activity_status = true
		and vt.vehicle_type_id = p_vehicle_type_id
		and not exists (
			select 1
			from booking b
			where b.driver_id = d.driver_id and b.booking_status in ('ACCEPTED','ARRIVED','IN_PROGRESS')
		)
		and (6371 * acos(
            cos(radians(lat)) * cos(radians(d.current_lat))
            * cos(radians(d.current_lng) - radians(lng))
            + sin(radians(lat)) * sin(radians(d.current_lat))
        )) <= radius
	order by Distance asc;
end;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account (
    account_id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL,
    user_name character varying(100) NOT NULL,
    pass_word character varying(100) NOT NULL,
    provider_id character varying(255),
    created_at timestamp(6) without time zone,
    account_status boolean,
    provider public.provider,
    fcm_token character varying(500)
);


--
-- Name: booking; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking (
    booking_id character varying(36) NOT NULL,
    customer_id character varying(36),
    driver_id character varying(36),
    payment_id character varying(36),
    promotion_id character varying(36),
    vehicle_type_id character varying(36),
    distance double precision,
    original_price double precision,
    total_price double precision,
    pickup_lat double precision,
    pickup_lng double precision,
    dropoff_lat double precision,
    dropoff_lng double precision,
    pickup_location character varying(255),
    dropoff_location character varying(255),
    booking_time timestamp(6) without time zone,
    pickup_time timestamp(6) without time zone,
    arrival_time timestamp(6) without time zone,
    booking_status public.bookingstatus,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: booking_promotion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking_promotion (
    id character varying(36) NOT NULL,
    booking_id character varying(36) NOT NULL,
    promotion_id character varying(36) NOT NULL,
    discount_amount double precision
);


--
-- Name: booking_rejection; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking_rejection (
    rejection_id character varying(36) NOT NULL,
    booking_id character varying(36) NOT NULL,
    driver_id character varying(36) NOT NULL,
    rejected_at timestamp(6) without time zone NOT NULL,
    rejection_type public.rejectiontype NOT NULL
);


--
-- Name: chat_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_message (
    id character varying(36) NOT NULL,
    booking_id character varying(36),
    sender_id character varying(36),
    receiver_id character varying(36),
    content text,
    "timestamp" timestamp without time zone
);


--
-- Name: customer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customer (
    customer_id character varying(36) NOT NULL,
    account_id character varying(36),
    phone character varying(15),
    customer_name character varying(255),
    birth_date date,
    address character varying(255),
    avatar character varying(255),
    email character varying(255),
    gender character varying(255)
);


--
-- Name: customer_promotion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customer_promotion (
    id character varying(36) NOT NULL,
    customer_id character varying(36),
    promotion_id character varying(36),
    saved_at timestamp(6) without time zone,
    used_at timestamp(6) without time zone,
    status public.customerpromotionstatus
);


--
-- Name: driver; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.driver (
    driver_id character varying(36) NOT NULL,
    account_id character varying(36),
    vehicle_type_id character varying(36),
    phone character varying(15),
    email character varying(100),
    citizen_id character varying(200),
    license_plate character varying(20),
    driver_name character varying(255),
    birth_date date,
    driving_license character varying(200),
    vehicle_name character varying(255),
    current_lat double precision,
    current_lng double precision,
    score double precision,
    last_trip_time timestamp(6) without time zone,
    address character varying(255),
    area character varying(255),
    avatar character varying(255),
    criminal_record character varying(255),
    gender character varying(255),
    activity_status boolean
);


--
-- Name: invalid_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invalid_token (
    id character varying(36) NOT NULL,
    expiry_time timestamp(6) without time zone NOT NULL,
    reason character varying(255) NOT NULL
);


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification (
    notification_id character varying(36) NOT NULL,
    account_id character varying(36),
    booking_id character varying(36),
    title character varying(255),
    message character varying(255),
    is_read boolean,
    sent_at timestamp(6) without time zone
);


--
-- Name: payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment (
    payment_id character varying(36) NOT NULL,
    amount double precision,
    payment_status boolean,
    payment_type public.paymentmethod
);


--
-- Name: promotion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.promotion (
    promotion_id character varying(36) NOT NULL,
    discount_limit double precision,
    discount_value double precision,
    is_active boolean,
    min_trip_value double precision,
    quantity integer,
    usage_limit_per_user integer,
    start_time timestamp(6) without time zone,
    end_time timestamp(6) without time zone,
    application_condition character varying(255),
    promotion_code character varying(255),
    promotion_image character varying(255),
    promotion_name character varying(255),
    discount_type public.discounttype,
    is_public boolean DEFAULT true
);


--
-- Name: rating; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rating (
    rating_id character varying(36) NOT NULL,
    booking_id character varying(36),
    score double precision,
    rating_type character varying(255),
    review character varying(255),
    created_at timestamp(6) without time zone
);


--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
    role_id character varying(36) NOT NULL,
    description character varying(255),
    role_name public.predefinedrole
);


--
-- Name: time; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."time" (
    time_id character varying(36) NOT NULL,
    start_time time(6) without time zone,
    end_time time(6) without time zone,
    slot_name character varying(255)
);


--
-- Name: vehicle_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vehicle_type (
    vehicle_type_id character varying(36) NOT NULL,
    max_passengers integer,
    price_per_km double precision,
    icon character varying(255),
    vehicle_type_name character varying(255)
);


--
-- Name: vehicle_type_time; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vehicle_type_time (
    time_id character varying(255) NOT NULL,
    vehicle_type_id character varying(255) NOT NULL,
    surcharge double precision
);


--
-- Name: wallet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallet (
    wallet_id character varying(36) NOT NULL,
    driver_id character varying(36),
    balance double precision,
    status public.walletstatus
);


--
-- Name: wallet_transaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallet_transaction (
    transaction_id character varying(36) NOT NULL,
    wallet_id character varying(36),
    reference_id character varying(255),
    amount double precision,
    created_at timestamp(6) without time zone,
    status public.transactionstatus,
    type public.transactiontype
);


--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (account_id);


--
-- Name: account account_user_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_user_name_key UNIQUE (user_name);


--
-- Name: booking booking_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_pkey PRIMARY KEY (booking_id);


--
-- Name: booking_promotion booking_promotion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_promotion
    ADD CONSTRAINT booking_promotion_pkey PRIMARY KEY (id);


--
-- Name: booking_rejection booking_rejection_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_rejection
    ADD CONSTRAINT booking_rejection_pkey PRIMARY KEY (rejection_id);


--
-- Name: chat_message chat_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_message
    ADD CONSTRAINT chat_message_pkey PRIMARY KEY (id);


--
-- Name: customer customer_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_account_id_key UNIQUE (account_id);


--
-- Name: customer customer_phone_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_phone_key UNIQUE (phone);


--
-- Name: customer customer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (customer_id);


--
-- Name: customer_promotion customer_promotion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_promotion
    ADD CONSTRAINT customer_promotion_pkey PRIMARY KEY (id);


--
-- Name: driver driver_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_account_id_key UNIQUE (account_id);


--
-- Name: driver driver_citizen_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_citizen_id_key UNIQUE (citizen_id);


--
-- Name: driver driver_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_email_key UNIQUE (email);


--
-- Name: driver driver_license_plate_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_license_plate_key UNIQUE (license_plate);


--
-- Name: driver driver_phone_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_phone_key UNIQUE (phone);


--
-- Name: driver driver_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_pkey PRIMARY KEY (driver_id);


--
-- Name: invalid_token invalid_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invalid_token
    ADD CONSTRAINT invalid_token_pkey PRIMARY KEY (id);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (notification_id);


--
-- Name: payment payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (payment_id);


--
-- Name: promotion promotion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promotion
    ADD CONSTRAINT promotion_pkey PRIMARY KEY (promotion_id);


--
-- Name: rating rating_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rating
    ADD CONSTRAINT rating_pkey PRIMARY KEY (rating_id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: time time_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."time"
    ADD CONSTRAINT time_pkey PRIMARY KEY (time_id);


--
-- Name: vehicle_type vehicle_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehicle_type
    ADD CONSTRAINT vehicle_type_pkey PRIMARY KEY (vehicle_type_id);


--
-- Name: vehicle_type_time vehicle_type_time_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehicle_type_time
    ADD CONSTRAINT vehicle_type_time_pkey PRIMARY KEY (time_id, vehicle_type_id);


--
-- Name: wallet wallet_driver_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallet
    ADD CONSTRAINT wallet_driver_id_key UNIQUE (driver_id);


--
-- Name: wallet wallet_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallet
    ADD CONSTRAINT wallet_pkey PRIMARY KEY (wallet_id);


--
-- Name: wallet_transaction wallet_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallet_transaction
    ADD CONSTRAINT wallet_transaction_pkey PRIMARY KEY (transaction_id);


--
-- Name: account account_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id);


--
-- Name: booking booking_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customer(customer_id);


--
-- Name: booking booking_driver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_driver_id_fkey FOREIGN KEY (driver_id) REFERENCES public.driver(driver_id);


--
-- Name: booking booking_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES public.payment(payment_id);


--
-- Name: booking booking_promotion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_promotion_id_fkey FOREIGN KEY (promotion_id) REFERENCES public.promotion(promotion_id);


--
-- Name: booking_rejection booking_rejection_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_rejection
    ADD CONSTRAINT booking_rejection_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.booking(booking_id);


--
-- Name: booking_rejection booking_rejection_driver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_rejection
    ADD CONSTRAINT booking_rejection_driver_id_fkey FOREIGN KEY (driver_id) REFERENCES public.driver(driver_id);


--
-- Name: booking booking_vehicle_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_vehicle_type_id_fkey FOREIGN KEY (vehicle_type_id) REFERENCES public.vehicle_type(vehicle_type_id);


--
-- Name: customer customer_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account(account_id);


--
-- Name: customer_promotion customer_promotion_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_promotion
    ADD CONSTRAINT customer_promotion_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customer(customer_id);


--
-- Name: customer_promotion customer_promotion_promotion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_promotion
    ADD CONSTRAINT customer_promotion_promotion_id_fkey FOREIGN KEY (promotion_id) REFERENCES public.promotion(promotion_id);


--
-- Name: driver driver_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account(account_id);


--
-- Name: driver driver_vehicle_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_vehicle_type_id_fkey FOREIGN KEY (vehicle_type_id) REFERENCES public.vehicle_type(vehicle_type_id);


--
-- Name: booking_promotion fk_booking; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_promotion
    ADD CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES public.booking(booking_id);


--
-- Name: booking_promotion fk_promotion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_promotion
    ADD CONSTRAINT fk_promotion FOREIGN KEY (promotion_id) REFERENCES public.promotion(promotion_id);


--
-- Name: notification notification_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account(account_id);


--
-- Name: notification notification_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.booking(booking_id);


--
-- Name: rating rating_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rating
    ADD CONSTRAINT rating_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.booking(booking_id);


--
-- Name: vehicle_type_time vehicle_type_time_time_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehicle_type_time
    ADD CONSTRAINT vehicle_type_time_time_id_fkey FOREIGN KEY (time_id) REFERENCES public."time"(time_id);


--
-- Name: vehicle_type_time vehicle_type_time_vehicle_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehicle_type_time
    ADD CONSTRAINT vehicle_type_time_vehicle_type_id_fkey FOREIGN KEY (vehicle_type_id) REFERENCES public.vehicle_type(vehicle_type_id);


--
-- Name: wallet wallet_driver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallet
    ADD CONSTRAINT wallet_driver_id_fkey FOREIGN KEY (driver_id) REFERENCES public.driver(driver_id);


--
-- Name: wallet_transaction wallet_transaction_wallet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallet_transaction
    ADD CONSTRAINT wallet_transaction_wallet_id_fkey FOREIGN KEY (wallet_id) REFERENCES public.wallet(wallet_id);


--
-- PostgreSQL database dump complete
--

