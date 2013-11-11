select concat(currency, '/', convert(holidayDate, varchar(255))) as "id",
currency as "currency",
holidayDate as "holidayDate"
from holiday