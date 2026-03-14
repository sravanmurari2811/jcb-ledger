package com.example.jcbledger.network;

import com.example.jcbledger.model.Customer;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.model.DriverExpense;
import com.example.jcbledger.model.User;
import com.example.jcbledger.dto.WorkEntryRequest;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/health/ping")
    Call<Map<String, String>> ping();

    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    @POST("api/auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> body);

    @GET("api/customers/mobile/{mobile}")
    Call<Customer> getCustomerByMobile(@Path("mobile") String mobile);

    @POST("api/work-entries")
    Call<WorkEntry> createWorkEntry(@Body WorkEntryRequest request, @Query("machineNumber") String machineNumber);

    @GET("api/work-entries/total-pending")
    Call<Map<String, Double>> getTotalPending(@Query("machineNumber") String machineNumber);

    @GET("api/work-entries/pending-bills")
    Call<List<WorkEntry>> getPendingBills(@Query("machineNumber") String machineNumber);

    @POST("api/work-entries/update-payment/{id}")
    Call<WorkEntry> updatePayment(@Path("id") Long id, @Body Map<String, Object> payload);

    @GET("api/work-entries/reports")
    Call<List<WorkEntry>> getReports(
            @Query("filter") String filter,
            @Query("statusFilter") String statusFilter,
            @Query("machineNumber") String machineNumber,
            @Query("date") String date);

    @GET("api/work-entries/customer/{mobile}")
    Call<List<WorkEntry>> getCustomerWorkEntries(
            @Path("mobile") String mobile,
            @Query("pendingOnly") boolean pendingOnly,
            @Query("machineNumber") String machineNumber);

    @POST("api/work-entries/receive-payment/{mobile}")
    Call<Map<String, Object>> receivePayment(
            @Path("mobile") String mobile,
            @Body Map<String, Object> payload);

    @GET("api/transactions/customer/{mobile}")
    Call<List<Map<String, Object>>> getCustomerTransactions(
            @Path("mobile") String mobile,
            @Query("machineNumber") String machineNumber);

    // Driver Expense Endpoints
    @GET("api/auth/operators")
    Call<List<Map<String, Object>>> getOperators(@Query("vehicleNumber") String vehicleNumber);

    @POST("api/driver-expenses")
    Call<DriverExpense> addDriverExpense(@Body DriverExpense expense);

    @GET("api/driver-expenses/operator/{operatorId}")
    Call<List<DriverExpense>> getDriverExpenses(@Path("operatorId") String operatorId);

    // Approval Endpoints
    @GET("api/auth/pending-approvals")
    Call<List<User>> getPendingApprovals(@Query("role") String role, @Query("vehicleNumber") String vehicleNumber);

    @POST("api/auth/approve-user/{id}")
    Call<Map<String, Object>> approveUser(@Path("id") Long id);

    // Admin User Management Endpoints
    @GET("api/auth/all-users")
    Call<List<User>> getAllUsers(@Query("role") String role);

    @POST("api/auth/update-user-status")
    Call<Map<String, Object>> updateUserStatus(@Body Map<String, Object> payload);
}
