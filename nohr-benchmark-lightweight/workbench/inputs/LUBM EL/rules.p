Professor(?Y) :- advisor(?X,?Y).
Organization(?Y) :- affiliatedOrganizationOf(?X,?Y).
Person(?Y) :- affiliateOf(?X,?Y).
University(?Y) :- degreeFrom(?X,?Y).
University(?Y) :- doctoralDegreeFrom(?X,?Y).
Person(?Y) :- hasAlumnus(?X,?Y).
Course(?Y) :- listedCourse(?X,?Y).
University(?Y) :- mastersDegreeFrom(?X,?Y).
Person(?Y) :- member(?X,?Y).
Publication(?Y) :- orgPublication(?X,?Y).
Person(?Y) :- publicationAuthor(?X,?Y).
Research(?Y) :- publicationResearch(?X,?Y).
Research(?Y) :- researchProject(?X,?Y).
Publication(?Y) :- softwareDocumentation(?X,?Y).
Organization(?Y) :- subOrganizationOf(?X,?Y).
Course(?Y) :- teacherOf(?X,?Y).
Course(?Y) :- teachingAssistantOf(?X,?Y).
University(?Y) :- undergraduateDegreeFrom(?X,?Y).
Person(?X) :- age(?X,?Y).
Person(?X) :- emailAddress(?X,?Y).
Person(?X) :- telephone(?X,?Y).
Person(?X) :- title(?X,?Y).
degreeFrom(?X,?Y) :- hasAlumnus(?Y,?X).
hasAlumnus(?X,?Y) :- degreeFrom(?Y,?X).
memberOf(?X,?Y) :- member(?Y,?X).
member(?X,?Y) :- memberOf(?Y,?X).