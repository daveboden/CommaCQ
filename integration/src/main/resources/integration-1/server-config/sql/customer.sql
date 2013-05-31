select CODE as "id", DESCRIPTION as "description",
ACTIVE as "active",
/*
case when ACTIVE = 1 then
    --Have to trim to 4 characters otherwise h2
    --makes it a char(5) with a trailing space
    convert('true', varchar(5)) 
else
    convert('false', varchar(5))
end "active",
*/
accountOpeningDate as "accountOpeningDate",
currentBalance as "currentBalance"
from customer