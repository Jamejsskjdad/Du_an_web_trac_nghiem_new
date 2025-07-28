export class UserProfile {
  id: number;
  firstName: string;
  lastName: string;
  icon?: string; // ✅ icon ảnh đại diện (ví dụ: 'avt1.png')

  constructor(firstName: string, lastName: string) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
