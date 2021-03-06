package com.probridge.vbox.model;

import java.io.Serializable;
import java.util.Date;

public class Course implements Serializable {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_id
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private String courseId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_name
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private String courseName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_description
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private String courseDescription;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_expiration
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private Date courseExpiration;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_preapprove_list
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private String coursePreapproveList;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_preapprove_quota
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private Integer coursePreapproveQuota;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_vm_golden_master
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private String courseVmGoldenMaster;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_vm_network
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private String courseVmNetwork;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_vm_cores
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private Integer courseVmCores;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column course_list.course_vm_memory
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private Integer courseVmMemory;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table course_list
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_id
     *
     * @return the value of course_list.course_id
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_id
     *
     * @param courseId the value for course_list.course_id
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId == null ? null : courseId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_name
     *
     * @return the value of course_list.course_name
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_name
     *
     * @param courseName the value for course_list.course_name
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName == null ? null : courseName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_description
     *
     * @return the value of course_list.course_description
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getCourseDescription() {
        return courseDescription;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_description
     *
     * @param courseDescription the value for course_list.course_description
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription == null ? null : courseDescription.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_expiration
     *
     * @return the value of course_list.course_expiration
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public Date getCourseExpiration() {
        return courseExpiration;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_expiration
     *
     * @param courseExpiration the value for course_list.course_expiration
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseExpiration(Date courseExpiration) {
        this.courseExpiration = courseExpiration;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_preapprove_list
     *
     * @return the value of course_list.course_preapprove_list
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getCoursePreapproveList() {
        return coursePreapproveList;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_preapprove_list
     *
     * @param coursePreapproveList the value for course_list.course_preapprove_list
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCoursePreapproveList(String coursePreapproveList) {
        this.coursePreapproveList = coursePreapproveList == null ? null : coursePreapproveList.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_preapprove_quota
     *
     * @return the value of course_list.course_preapprove_quota
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public Integer getCoursePreapproveQuota() {
        return coursePreapproveQuota;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_preapprove_quota
     *
     * @param coursePreapproveQuota the value for course_list.course_preapprove_quota
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCoursePreapproveQuota(Integer coursePreapproveQuota) {
        this.coursePreapproveQuota = coursePreapproveQuota;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_vm_golden_master
     *
     * @return the value of course_list.course_vm_golden_master
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getCourseVmGoldenMaster() {
        return courseVmGoldenMaster;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_vm_golden_master
     *
     * @param courseVmGoldenMaster the value for course_list.course_vm_golden_master
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseVmGoldenMaster(String courseVmGoldenMaster) {
        this.courseVmGoldenMaster = courseVmGoldenMaster == null ? null : courseVmGoldenMaster.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_vm_network
     *
     * @return the value of course_list.course_vm_network
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public String getCourseVmNetwork() {
        return courseVmNetwork;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_vm_network
     *
     * @param courseVmNetwork the value for course_list.course_vm_network
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseVmNetwork(String courseVmNetwork) {
        this.courseVmNetwork = courseVmNetwork == null ? null : courseVmNetwork.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_vm_cores
     *
     * @return the value of course_list.course_vm_cores
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public Integer getCourseVmCores() {
        return courseVmCores;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_vm_cores
     *
     * @param courseVmCores the value for course_list.course_vm_cores
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseVmCores(Integer courseVmCores) {
        this.courseVmCores = courseVmCores;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column course_list.course_vm_memory
     *
     * @return the value of course_list.course_vm_memory
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public Integer getCourseVmMemory() {
        return courseVmMemory;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column course_list.course_vm_memory
     *
     * @param courseVmMemory the value for course_list.course_vm_memory
     *
     * @mbggenerated Sun Jan 19 09:59:57 CST 2014
     */
    public void setCourseVmMemory(Integer courseVmMemory) {
        this.courseVmMemory = courseVmMemory;
    }
}