import 'dart:convert';
import 'dart:async';
import 'package:http/http.dart' as http;
import '../models/user.dart';

class AuthService {
  static const String baseUrl = 'http://10.138.222.95:8080/api/auth';
  
  // Headers for API requests
  Map<String, String> get _headers => {
    'Content-Type': 'application/json',
  };

  Map<String, String> _authHeaders(String token) => {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer $token',
  };

  // Debug method to log auth headers
  void logAuthHeaders(String token) {
    print('AuthService: Auth headers: ${_authHeaders(token)}');
  }

  // Register new user
  Future<AuthResponse> register(RegistrationRequest request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/register'),
        headers: _headers,
        body: jsonEncode(request.toJson()),
      );

      print('Registration response status: ${response.statusCode}');
      print('Registration response body: ${response.body}');

      if (response.statusCode == 201) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Registration successful',
          user: data['data'] != null ? User.fromJson(data['data']['user']) : null,
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Registration failed',
        );
      }
    } catch (e) {
      print('Registration error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Login user
  Future<AuthResponse> login(LoginRequest request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/login'),
        headers: _headers,
        body: jsonEncode(request.toJson()),
      );

      print('Login response status: ${response.statusCode}');
      print('Login response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Login successful',
          user: data['data'] != null ? User.fromJson(data['data']['user']) : null,
          accessToken: data['data']?['accessToken'],
          refreshToken: data['data']?['refreshToken'],
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Login failed',
        );
      }
    } catch (e) {
      print('Login error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Get User Profile
  Future<AuthResponse> getUserProfile(String accessToken) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/profile'),
        headers: _authHeaders(accessToken),
      );

      print('Profile response status: ${response.statusCode}');
      print('Profile response body: ${response.body}');

      if (response.statusCode == 200) {
        if (response.body.isEmpty) {
          return AuthResponse(
            success: false,
            message: 'Empty response from server',
          );
        }
        
        try {
          final data = jsonDecode(response.body);
          if (data['success'] == true && data['data'] != null) {
            return AuthResponse(
              success: true,
              message: data['message'] ?? 'Profile loaded successfully',
              user: User.fromJson(data['data']),
            );
          } else {
            return AuthResponse(
              success: false,
              message: data['message'] ?? 'Failed to load profile',
            );
          }
        } catch (e) {
          print('Profile JSON decode error: $e');
          return AuthResponse(
            success: false,
            message: 'Invalid response format from server',
          );
        }
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Failed to load profile',
        );
      }
    } catch (e) {
      print('Profile error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Update User Profile
  Future<AuthResponse> updateProfile(String accessToken, Map<String, dynamic> profileData) async {
    try {
      final response = await http.put(
        Uri.parse('$baseUrl/profile'),
        headers: _authHeaders(accessToken),
        body: jsonEncode(profileData),
      );

      print('Profile update response status: ${response.statusCode}');
      print('Profile update response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Profile updated successfully',
          user: data['data'] != null ? User.fromJson(data['data']) : null,
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Profile update failed',
        );
      }
    } catch (e) {
      print('Profile update error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Logout
  Future<AuthResponse> logout(String accessToken, HttpServletRequest request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/logout'),
        headers: _authHeaders(accessToken),
      );

      print('Logout response status: ${response.statusCode}');
      print('Logout response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Logout successful',
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Logout failed',
        );
      }
    } catch (e) {
      print('Logout error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Logout from all devices
  Future<AuthResponse> logoutFromAllDevices(String accessToken) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/logout-all'),
        headers: _authHeaders(accessToken),
      );

      print('Logout all devices response status: ${response.statusCode}');
      print('Logout all devices response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Logged out from all devices successfully',
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Logout from all devices failed',
        );
      }
    } catch (e) {
      print('Logout all devices error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Get trusted devices
  Future<AuthResponse> getTrustedDevices(String accessToken) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/trusted-devices'),
        headers: _authHeaders(accessToken),
      );

      print('Trusted devices response status: ${response.statusCode}');
      print('Trusted devices response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Trusted devices loaded successfully',
          data: data['data'],
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Failed to load trusted devices',
        );
      }
    } catch (e) {
      print('Trusted devices error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Revoke trusted device
  Future<AuthResponse> revokeTrustedDevice(String accessToken, int deviceId) async {
    try {
      final response = await http.delete(
        Uri.parse('$baseUrl/trusted-devices/$deviceId'),
        headers: _authHeaders(accessToken),
      );

      print('Revoke device response status: ${response.statusCode}');
      print('Revoke device response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Device revoked successfully',
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Device revocation failed',
        );
      }
    } catch (e) {
      print('Revoke device error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Forgot password
  Future<AuthResponse> forgotPassword(String email) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/forgot-password'),
        headers: _headers,
        body: jsonEncode({'email': email}),
      );

      print('Forgot password response status: ${response.statusCode}');
      print('Forgot password response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Password reset email sent',
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Password reset request failed',
        );
      }
    } catch (e) {
      print('Forgot password error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Reset password
  Future<AuthResponse> resetPassword(String token, String newPassword) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/reset-password'),
        headers: _headers,
        body: jsonEncode({
          'token': token,
          'newPassword': newPassword,
        }),
      );

      print('Reset password response status: ${response.statusCode}');
      print('Reset password response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Password reset successfully',
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Password reset failed',
        );
      }
    } catch (e) {
      print('Reset password error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Verify email with token
  Future<AuthResponse> verifyEmail(String token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/verify-email?token=$token'),
        headers: _headers,
      );

      print('Email verification response status: ${response.statusCode}');
      print('Email verification response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Email verified successfully',
          user: data['data'] != null ? User.fromJson(data['data']['user']) : null,
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Email verification failed',
        );
      }
    } catch (e) {
      print('Email verification error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }

  // Resend verification email
  Future<AuthResponse> resendVerificationEmail(String email) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/resend-verification'),
        headers: _headers,
        body: jsonEncode({'email': email}),
      );

      print('Resend verification response status: ${response.statusCode}');
      print('Resend verification response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: true,
          message: data['message'] ?? 'Verification email sent successfully',
        );
      } else {
        final data = jsonDecode(response.body);
        return AuthResponse(
          success: false,
          message: data['message'] ?? 'Failed to resend verification email',
        );
      }
    } catch (e) {
      print('Resend verification error: $e');
      return AuthResponse(
        success: false,
        message: 'Network error: $e',
      );
    }
  }
}

// Data classes for requests and responses
class RegistrationRequest {
  final String username;
  final String email;
  final String password;
  final String firstName;
  final String? lastName;
  final String apartmentNumber;
  final String buildingName;
  final String? phoneNumber;

  RegistrationRequest({
    required this.username,
    required this.email,
    required this.password,
    required this.firstName,
    this.lastName,
    required this.apartmentNumber,
    required this.buildingName,
    this.phoneNumber,
  });

  Map<String, dynamic> toJson() => {
    'username': username,
    'email': email,
    'password': password,
    'firstName': firstName,
    'lastName': lastName,
    'apartmentNumber': apartmentNumber,
    'buildingName': buildingName,
    'phoneNumber': phoneNumber,
  };
}

class LoginRequest {
  final String username;
  final String password;

  LoginRequest({
    required this.username,
    required this.password,
  });

  Map<String, dynamic> toJson() => {
    'username': username,
    'password': password,
  };
}

class AuthResponse {
  final bool success;
  final String message;
  final User? user;
  final String? accessToken;
  final String? refreshToken;
  final dynamic data;

  AuthResponse({
    required this.success,
    required this.message,
    this.user,
    this.accessToken,
    this.refreshToken,
    this.data,
  });
}

// Simple HTTP request class for logout
class HttpServletRequest {
  // This is a placeholder for the logout method
  // In a real implementation, you might need to handle device fingerprinting
}
