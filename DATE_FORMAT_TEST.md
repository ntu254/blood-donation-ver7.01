# 🧪 KIỂM TRA DATE FORMAT COMPATIBILITY

## ⚠️ VẤN ĐỀ PHÁT HIỆN

### **Backend RegisterRequest.java:**
```java
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
private LocalDate dateOfBirth;
```

### **Frontend useRegister.js:**
```javascript
// Frontend gửi format: YYYY-MM-DD
const convertDateFormat = (dateString) => {
  // Convert from DD-MM-YYYY (display) to YYYY-MM-DD (ISO)
  const [day, month, year] = dateString.split('-');
  return `${year}-${formattedMonth}-${formattedDay}`;
};

const registrationData = {
  dateOfBirth, // ISO format string YYYY-MM-DD
};
```

## 🔍 PHÂN TÍCH CONFLICT

### **Vấn đề:**
- **Backend expects**: `dd-MM-yyyy` format (VD: "25-12-1990")
- **Frontend sends**: `yyyy-MM-dd` format (VD: "1990-12-25")

### **Root Cause:**
Jackson annotation `@JsonFormat(pattern = "dd-MM-yyyy")` yêu cầu backend nhận date theo format DD-MM-YYYY, nhưng frontend đang gửi theo format YYYY-MM-DD.

## 🔧 GIẢI PHÁP ĐƯỢC ĐỀ XUẤT

### **Option 1: Sửa Backend (Recommended)**
```java
// BEFORE
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
private LocalDate dateOfBirth;

// AFTER
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
private LocalDate dateOfBirth;
```

### **Option 2: Sửa Frontend**
```javascript
// Thay đổi logic conversion
const convertDateFormat = (dateString) => {
  // Input: DD-MM-YYYY, Output: DD-MM-YYYY (không convert)
  return dateString; // Giữ nguyên format DD-MM-YYYY
};
```

### **Option 3: Xóa @JsonFormat Annotation (Simplest)**
```java
// Để Spring Boot tự động parse ISO format
@NotNull(message = "Date of birth is required")
@Past(message = "Date of birth must be in the past")
private LocalDate dateOfBirth; // Sẽ accept YYYY-MM-DD automatically
```

## 🎯 KHUYẾN NGHỊ

**Chọn Option 3** - Xóa `@JsonFormat` annotation vì:

1. ✅ **Simplest solution**: Ít thay đổi code nhất
2. ✅ **Spring Boot default**: LocalDate tự động parse ISO format (YYYY-MM-DD)
3. ✅ **Frontend compatibility**: Không cần sửa logic frontend
4. ✅ **Standard practice**: ISO format là standard cho date API
5. ✅ **AdminCreateUserRequest**: Cũng không có @JsonFormat annotation

## 🚀 IMPLEMENTATION

### **File cần sửa:**
`src/main/java/com/hicode/backend/dto/RegisterRequest.java`

### **Thay đổi:**
```java
@NotNull(message = "Date of birth is required")
@Past(message = "Date of birth must be in the past")
// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy") // XÓA DÒNG NÀY
private LocalDate dateOfBirth;
```

## ✅ EXPECTED RESULT

Sau khi sửa:
- Frontend gửi: `"dateOfBirth": "1990-12-25"`
- Backend nhận: `LocalDate.of(1990, 12, 25)`
- Status: ✅ **CONSISTENT**

## 📋 TESTING

### **Test case:**
```javascript
// Frontend
formData.dateOfBirth = "25-12-1990" // User input
convertedDate = "1990-12-25" // After conversion
// Send to backend: {"dateOfBirth": "1990-12-25"}

// Backend after fix
// Will parse "1990-12-25" to LocalDate(1990, 12, 25) automatically
```

### **Verification:**
1. User registers with birthday: 25-12-1990
2. Frontend converts to: 1990-12-25
3. Backend parses successfully without @JsonFormat
4. Database stores: 1990-12-25
5. ✅ Success!
