# Profile Picture Upload - Frontend Integration Guide

## Backend API Endpoints Available

### 1. Upload Profile Picture
```
POST /users/me/profile-picture
Content-Type: multipart/form-data
Authorization: Bearer <JWT_TOKEN>

Request Body:
- file: (Binary file - image only)

Response: 200 OK
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "profilePictureUrl": "/uploads/profile-pictures/profile_1_uuid-here.jpg",
  "role": "FREELANCER",
  ...other user fields
}

Error Responses:
- 400: Invalid file type (only images allowed)
- 400: File too large (max 5MB)
- 400: Empty file
- 401: Unauthorized
```

### 2. Delete Profile Picture
```
DELETE /users/me/profile-picture
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "profilePictureUrl": null,
  ...other user fields
}

Error Responses:
- 401: Unauthorized
```

### 3. Access Profile Picture
```
GET /uploads/profile-pictures/profile_1_uuid-here.jpg

Response: Image file (publicly accessible)
```

## Frontend Implementation Requirements

### Task: Implement Profile Picture Upload Feature

Please implement a complete profile picture upload feature in the frontend with the following requirements:

#### 1. **Profile Picture Upload Component**
   - Create a file input component that accepts only image files (jpg, jpeg, png, gif, webp)
   - Display current profile picture if exists, or a default avatar placeholder
   - Add an "Upload Photo" or camera icon button to trigger file selection
   - Show a preview of the selected image before uploading
   - Include a loading spinner during upload
   - Display success/error messages after upload attempt

#### 2. **File Validation (Client-Side)**
   - Validate file type: Only allow image files (check MIME type and extension)
   - Validate file size: Maximum 5MB
   - Show user-friendly error messages for validation failures
   - Example error messages:
     - "Please select an image file (JPG, PNG, GIF, or WebP)"
     - "File size must be less than 5MB"

#### 3. **API Integration**
   - Use `FormData` to send the file as multipart/form-data
   - Include JWT token in Authorization header
   - Handle the response to update the user's profile picture URL in state/context
   - Construct full image URL: `${BASE_URL}${profilePictureUrl}` 
     (e.g., `http://localhost:8080/uploads/profile-pictures/profile_1_uuid.jpg`)

#### 4. **Delete Profile Picture**
   - Add a "Remove Photo" or trash icon button when profile picture exists
   - Confirm deletion with user before sending DELETE request
   - Update UI to show default avatar after successful deletion

#### 5. **UI/UX Considerations**
   - Display profile picture in a circular avatar (e.g., 150x150px in profile page, 40x40px in header)
   - Use object-fit: cover to maintain aspect ratio
   - Add hover effect on avatar to show upload/change option
   - Consider adding image cropping functionality (optional but recommended)
   - Handle broken image URLs gracefully with fallback to default avatar

#### 6. **Error Handling**
   - Network errors: "Failed to upload. Please check your connection."
   - Unauthorized: Redirect to login or refresh token
   - File too large: "Image must be less than 5MB"
   - Invalid file type: "Please upload a valid image file"
   - Server errors: "Upload failed. Please try again."

## Example Implementation Code

### React Example (with fetch):

```javascript
// Upload Profile Picture
const uploadProfilePicture = async (file) => {
  // Validate file
  if (!file.type.startsWith('image/')) {
    alert('Please select an image file');
    return;
  }
  
  if (file.size > 5 * 1024 * 1024) {
    alert('File size must be less than 5MB');
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await fetch('http://localhost:8080/users/me/profile-picture', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formData
    });

    if (!response.ok) {
      throw new Error('Upload failed');
    }

    const userData = await response.json();
    // Update user context/state with new profile picture URL
    setUser(userData);
    
    alert('Profile picture updated successfully!');
  } catch (error) {
    console.error('Error uploading profile picture:', error);
    alert('Failed to upload profile picture. Please try again.');
  }
};

// Delete Profile Picture
const deleteProfilePicture = async () => {
  if (!confirm('Are you sure you want to remove your profile picture?')) {
    return;
  }

  try {
    const response = await fetch('http://localhost:8080/users/me/profile-picture', {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (!response.ok) {
      throw new Error('Delete failed');
    }

    const userData = await response.json();
    setUser(userData);
    
    alert('Profile picture removed successfully!');
  } catch (error) {
    console.error('Error deleting profile picture:', error);
    alert('Failed to remove profile picture. Please try again.');
  }
};

// Display Profile Picture
const ProfileAvatar = ({ user, size = 150 }) => {
  const imageUrl = user.profilePictureUrl 
    ? `http://localhost:8080${user.profilePictureUrl}`
    : '/default-avatar.png';

  return (
    <img 
      src={imageUrl}
      alt={`${user.firstName} ${user.lastName}`}
      style={{
        width: size,
        height: size,
        borderRadius: '50%',
        objectFit: 'cover'
      }}
      onError={(e) => {
        e.target.src = '/default-avatar.png';
      }}
    />
  );
};
```

### Angular Example:

```typescript
// profile.service.ts
uploadProfilePicture(file: File): Observable<User> {
  // Validate file
  if (!file.type.startsWith('image/')) {
    throw new Error('Please select an image file');
  }
  
  if (file.size > 5 * 1024 * 1024) {
    throw new Error('File size must be less than 5MB');
  }

  const formData = new FormData();
  formData.append('file', file);

  return this.http.post<User>(
    `${this.apiUrl}/users/me/profile-picture`,
    formData,
    {
      headers: {
        'Authorization': `Bearer ${this.getToken()}`
      }
    }
  );
}

deleteProfilePicture(): Observable<User> {
  return this.http.delete<User>(
    `${this.apiUrl}/users/me/profile-picture`,
    {
      headers: {
        'Authorization': `Bearer ${this.getToken()}`
      }
    }
  );
}

getProfilePictureUrl(profilePictureUrl: string | null): string {
  return profilePictureUrl 
    ? `${this.apiUrl}${profilePictureUrl}`
    : 'assets/images/default-avatar.png';
}
```

### Vue Example:

```javascript
// composables/useProfilePicture.js
export const useProfilePicture = () => {
  const uploadProfilePicture = async (file) => {
    if (!file.type.startsWith('image/')) {
      throw new Error('Please select an image file');
    }
    
    if (file.size > 5 * 1024 * 1024) {
      throw new Error('File size must be less than 5MB');
    }

    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('http://localhost:8080/users/me/profile-picture', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formData
    });

    if (!response.ok) {
      throw new Error('Upload failed');
    }

    return response.json();
  };

  const deleteProfilePicture = async () => {
    const response = await fetch('http://localhost:8080/users/me/profile-picture', {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (!response.ok) {
      throw new Error('Delete failed');
    }

    return response.json();
  };

  const getProfilePictureUrl = (profilePictureUrl) => {
    return profilePictureUrl 
      ? `http://localhost:8080${profilePictureUrl}`
      : '/default-avatar.png';
  };

  return {
    uploadProfilePicture,
    deleteProfilePicture,
    getProfilePictureUrl
  };
};
```

## Testing Checklist

- [ ] Can upload a valid image file (JPG, PNG, GIF, WebP)
- [ ] Cannot upload non-image files (PDF, DOC, etc.)
- [ ] Cannot upload files larger than 5MB
- [ ] Profile picture displays correctly after upload
- [ ] Profile picture URL is saved and persists after page reload
- [ ] Can delete profile picture and it shows default avatar
- [ ] Broken image links fallback to default avatar
- [ ] Loading states work correctly
- [ ] Error messages are user-friendly
- [ ] Works on mobile devices
- [ ] Image maintains aspect ratio with object-fit: cover

## Notes

1. **Base URL Configuration**: Make sure to use environment variables for the base URL:
   - Development: `http://localhost:8080`
   - Production: Your production API URL

2. **CORS**: The backend is already configured to accept requests from your frontend origin

3. **Authentication**: All requests must include the JWT token in the Authorization header

4. **Image Optimization**: Consider resizing large images on the client-side before upload to improve performance

5. **Caching**: Profile pictures are served as static resources, so browser caching will work automatically

6. **Default Avatar**: Make sure to have a default avatar image in your assets folder

## Questions or Issues?

If you encounter any issues:
1. Check the browser console for errors
2. Check network tab to see the actual request/response
3. Verify the JWT token is being sent correctly
4. Verify the file meets size and type requirements
5. Check backend logs for detailed error messages

The backend implementation includes comprehensive logging, so check the server logs if something isn't working as expected.
