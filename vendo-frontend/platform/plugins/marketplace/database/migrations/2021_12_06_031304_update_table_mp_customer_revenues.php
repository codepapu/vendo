<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class () extends Migration {
    public function up(): void
    {
        $connection = Schema::getConnection();
        $driver = $connection->getDriverName();
        
        // Check if columns need to be updated by checking if they are still unsigned
        if ($driver === 'sqlite') {
            // SQLite doesn't support DESCRIBE or changing column types easily
            // For SQLite, we'll skip this migration as it's not critical
            // SQLite doesn't enforce unsigned constraints anyway
            return;
        }
        
        $columns = DB::select('DESCRIBE mp_customer_revenues');
        $needsUpdate = false;

        foreach ($columns as $column) {
            if (in_array($column->Field, ['sub_amount', 'amount', 'current_balance']) &&
                str_contains($column->Type, 'unsigned')) {
                $needsUpdate = true;

                break;
            }
        }

        if (! $needsUpdate) {
            return;
        }

        Schema::table('mp_customer_revenues', function (Blueprint $table): void {
            $table->decimal('sub_amount', 15)->default(0)->nullable()->change();
            $table->decimal('amount', 15)->default(0)->nullable()->change();
            $table->decimal('current_balance', 15)->default(0)->nullable()->change();
        });
    }

    public function down(): void
    {
        Schema::table('mp_customer_revenues', function (Blueprint $table): void {
            $table->decimal('sub_amount', 15)->default(0)->unsigned()->nullable()->change();
            $table->decimal('amount', 15)->default(0)->unsigned()->nullable()->change();
            $table->decimal('current_balance', 15)->default(0)->unsigned()->nullable()->change();
        });
    }
};
