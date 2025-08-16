class User {
  final int id;
  final String username;
  final String? firstName;
  final String? lastName;
  final String? displayName;
  final String email;
  final bool emailVerified;
  final bool phoneVerified;
  final String? apartmentNumber;
  final String? buildingName;
  final String? phoneNumber;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  User({
    required this.id,
    required this.username,
    this.firstName,
    this.lastName,
    this.displayName,
    required this.email,
    required this.emailVerified,
    required this.phoneVerified,
    this.apartmentNumber,
    this.buildingName,
    this.phoneNumber,
    this.createdAt,
    this.updatedAt,
  });

  factory User.fromJson(Map<String, dynamic>? json) {
    if (json == null) {
      print('User.fromJson called with null JSON data');
      return User(
        id: 0,
        username: 'null_user',
        email: 'null@example.com',
        emailVerified: false,
        phoneVerified: false,
        createdAt: null,
        updatedAt: null,
      );
    }
    
    try {
      return User(
        id: json['id'] != null ? (json['id'] is int ? json['id'] : json['id'].toInt()) : 0,
        username: json['username']?.toString() ?? '',
        firstName: json['firstName']?.toString(),
        lastName: json['lastName']?.toString(),
        displayName: json['displayName']?.toString(),
        email: json['email']?.toString() ?? '',
        emailVerified: json['emailVerified'] == true,
        phoneVerified: json['phoneVerified'] == true,
        apartmentNumber: json['apartmentNumber']?.toString(),
        buildingName: json['buildingName']?.toString(),
        phoneNumber: json['phoneNumber']?.toString(),
        createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'].toString()) : null,
        updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt'].toString()) : null,
      );
    } catch (e) {
      print('Error parsing User from JSON: $e');
      print('JSON data: $json');
      // Return a default user to prevent crashes
      return User(
        id: 0,
        username: 'error_user',
        email: 'error@example.com',
        emailVerified: false,
        phoneVerified: false,
        createdAt: null,
        updatedAt: null,
      );
    }
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'firstName': firstName,
      'lastName': lastName,
      'displayName': displayName,
      'email': email,
      'emailVerified': emailVerified,
      'phoneVerified': phoneVerified,
      'apartmentNumber': apartmentNumber,
      'buildingName': buildingName,
      'phoneNumber': phoneNumber,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  // Safe getter methods with null checks
  String get fullName {
    if (firstName != null && lastName != null) {
      return '$firstName $lastName';
    } else if (firstName != null) {
      return firstName!;
    } else if (displayName != null) {
      return displayName!;
    }
    return username;
  }

  User copyWith({
    int? id,
    String? username,
    String? firstName,
    String? lastName,
    String? displayName,
    String? email,
    bool? emailVerified,
    bool? phoneVerified,
    String? apartmentNumber,
    String? buildingName,
    String? phoneNumber,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return User(
      id: id ?? this.id,
      username: username ?? this.username,
      firstName: firstName ?? this.firstName,
      lastName: lastName ?? this.lastName,
      displayName: displayName ?? this.displayName,
      email: email ?? this.email,
      emailVerified: emailVerified ?? this.emailVerified,
      phoneVerified: phoneVerified ?? this.phoneVerified,
      apartmentNumber: apartmentNumber ?? this.apartmentNumber,
      buildingName: buildingName ?? this.buildingName,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  String toString() {
    return 'User(id: $id, username: $username, firstName: $firstName, lastName: $lastName, displayName: $displayName, email: $email, emailVerified: $emailVerified, phoneVerified: $phoneVerified, apartmentNumber: $apartmentNumber, buildingName: $buildingName, phoneNumber: $phoneNumber, createdAt: $createdAt, updatedAt: $updatedAt)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is User &&
        other.id == id &&
        other.username == username &&
        other.firstName == firstName &&
        other.lastName == lastName &&
        other.displayName == displayName &&
        other.email == email &&
        other.emailVerified == emailVerified &&
        other.phoneVerified == phoneVerified &&
        other.apartmentNumber == apartmentNumber &&
        other.buildingName == buildingName &&
        other.phoneNumber == phoneNumber &&
        other.createdAt == createdAt &&
        other.updatedAt == updatedAt;
  }

  @override
  int get hashCode {
    return id.hashCode ^
        username.hashCode ^
        firstName.hashCode ^
        lastName.hashCode ^
        displayName.hashCode ^
        email.hashCode ^
        emailVerified.hashCode ^
        phoneVerified.hashCode ^
        apartmentNumber.hashCode ^
        buildingName.hashCode ^
        phoneNumber.hashCode ^
        createdAt.hashCode ^
        updatedAt.hashCode;
  }
}
