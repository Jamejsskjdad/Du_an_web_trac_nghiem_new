import { Intake } from './intake';

export class Course {
  id: number;
  courseCode: string;
  name: string;
  intakes?: Intake[]; // ✅ Thêm dòng này

  constructor(courseCode: string, name: string) {
    this.courseCode = courseCode;
    this.name = name;
  }
}
