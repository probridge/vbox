package com.probridge.vbox.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GMImageExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public GMImageExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andGmImageIdIsNull() {
            addCriterion("gm_image_id is null");
            return (Criteria) this;
        }

        public Criteria andGmImageIdIsNotNull() {
            addCriterion("gm_image_id is not null");
            return (Criteria) this;
        }

        public Criteria andGmImageIdEqualTo(Integer value) {
            addCriterion("gm_image_id =", value, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdNotEqualTo(Integer value) {
            addCriterion("gm_image_id <>", value, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdGreaterThan(Integer value) {
            addCriterion("gm_image_id >", value, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("gm_image_id >=", value, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdLessThan(Integer value) {
            addCriterion("gm_image_id <", value, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdLessThanOrEqualTo(Integer value) {
            addCriterion("gm_image_id <=", value, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdIn(List<Integer> values) {
            addCriterion("gm_image_id in", values, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdNotIn(List<Integer> values) {
            addCriterion("gm_image_id not in", values, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdBetween(Integer value1, Integer value2) {
            addCriterion("gm_image_id between", value1, value2, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageIdNotBetween(Integer value1, Integer value2) {
            addCriterion("gm_image_id not between", value1, value2, "gmImageId");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameIsNull() {
            addCriterion("gm_image_filename is null");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameIsNotNull() {
            addCriterion("gm_image_filename is not null");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameEqualTo(String value) {
            addCriterion("gm_image_filename =", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameNotEqualTo(String value) {
            addCriterion("gm_image_filename <>", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameGreaterThan(String value) {
            addCriterion("gm_image_filename >", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameGreaterThanOrEqualTo(String value) {
            addCriterion("gm_image_filename >=", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameLessThan(String value) {
            addCriterion("gm_image_filename <", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameLessThanOrEqualTo(String value) {
            addCriterion("gm_image_filename <=", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameLike(String value) {
            addCriterion("gm_image_filename like", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameNotLike(String value) {
            addCriterion("gm_image_filename not like", value, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameIn(List<String> values) {
            addCriterion("gm_image_filename in", values, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameNotIn(List<String> values) {
            addCriterion("gm_image_filename not in", values, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameBetween(String value1, String value2) {
            addCriterion("gm_image_filename between", value1, value2, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageFilenameNotBetween(String value1, String value2) {
            addCriterion("gm_image_filename not between", value1, value2, "gmImageFilename");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionIsNull() {
            addCriterion("gm_image_description is null");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionIsNotNull() {
            addCriterion("gm_image_description is not null");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionEqualTo(String value) {
            addCriterion("gm_image_description =", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionNotEqualTo(String value) {
            addCriterion("gm_image_description <>", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionGreaterThan(String value) {
            addCriterion("gm_image_description >", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("gm_image_description >=", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionLessThan(String value) {
            addCriterion("gm_image_description <", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionLessThanOrEqualTo(String value) {
            addCriterion("gm_image_description <=", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionLike(String value) {
            addCriterion("gm_image_description like", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionNotLike(String value) {
            addCriterion("gm_image_description not like", value, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionIn(List<String> values) {
            addCriterion("gm_image_description in", values, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionNotIn(List<String> values) {
            addCriterion("gm_image_description not in", values, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionBetween(String value1, String value2) {
            addCriterion("gm_image_description between", value1, value2, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageDescriptionNotBetween(String value1, String value2) {
            addCriterion("gm_image_description not between", value1, value2, "gmImageDescription");
            return (Criteria) this;
        }

        public Criteria andGmImageLockIsNull() {
            addCriterion("gm_image_lock is null");
            return (Criteria) this;
        }

        public Criteria andGmImageLockIsNotNull() {
            addCriterion("gm_image_lock is not null");
            return (Criteria) this;
        }

        public Criteria andGmImageLockEqualTo(String value) {
            addCriterion("gm_image_lock =", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockNotEqualTo(String value) {
            addCriterion("gm_image_lock <>", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockGreaterThan(String value) {
            addCriterion("gm_image_lock >", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockGreaterThanOrEqualTo(String value) {
            addCriterion("gm_image_lock >=", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockLessThan(String value) {
            addCriterion("gm_image_lock <", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockLessThanOrEqualTo(String value) {
            addCriterion("gm_image_lock <=", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockLike(String value) {
            addCriterion("gm_image_lock like", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockNotLike(String value) {
            addCriterion("gm_image_lock not like", value, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockIn(List<String> values) {
            addCriterion("gm_image_lock in", values, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockNotIn(List<String> values) {
            addCriterion("gm_image_lock not in", values, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockBetween(String value1, String value2) {
            addCriterion("gm_image_lock between", value1, value2, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageLockNotBetween(String value1, String value2) {
            addCriterion("gm_image_lock not between", value1, value2, "gmImageLock");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateIsNull() {
            addCriterion("gm_image_creation_date is null");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateIsNotNull() {
            addCriterion("gm_image_creation_date is not null");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateEqualTo(Date value) {
            addCriterion("gm_image_creation_date =", value, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateNotEqualTo(Date value) {
            addCriterion("gm_image_creation_date <>", value, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateGreaterThan(Date value) {
            addCriterion("gm_image_creation_date >", value, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateGreaterThanOrEqualTo(Date value) {
            addCriterion("gm_image_creation_date >=", value, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateLessThan(Date value) {
            addCriterion("gm_image_creation_date <", value, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateLessThanOrEqualTo(Date value) {
            addCriterion("gm_image_creation_date <=", value, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateIn(List<Date> values) {
            addCriterion("gm_image_creation_date in", values, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateNotIn(List<Date> values) {
            addCriterion("gm_image_creation_date not in", values, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateBetween(Date value1, Date value2) {
            addCriterion("gm_image_creation_date between", value1, value2, "gmImageCreationDate");
            return (Criteria) this;
        }

        public Criteria andGmImageCreationDateNotBetween(Date value1, Date value2) {
            addCriterion("gm_image_creation_date not between", value1, value2, "gmImageCreationDate");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table golden_master_images
     *
     * @mbggenerated do_not_delete_during_merge Sun Jan 19 09:59:57 CST 2014
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table golden_master_images
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}